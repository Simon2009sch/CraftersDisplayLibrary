package me.simoncrafter.CraftersDisplayLibrary.effect.internal;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal base for the "timed effect" registry pattern shared by
 * {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.BlockHighlighter} and
 * {@link me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.ViewTinter}: a registry keyed by
 * {@code K}, tracking per key a display of type {@code D}, an {@link EffectFunction} driving it on
 * a repeating tick task, and an optional lifetime after which the entry auto-removes itself.
 * <p>
 * Subclasses (typically an anonymous instance held via composition, not inheritance, by the public
 * static facade class) implement {@link #onRemove(Object, IColorableDisplay)} for the effect-specific
 * teardown that runs in addition to this class's own bookkeeping (stopping the tick task, cancelling
 * any in-flight colour animation via {@link GlobalAnimationTickHandler#removeColorAnimation}, and
 * removing the registry entry).
 * <p>
 * A single shared background task per registry instance lazily starts the first time an entry is
 * registered with {@code lifeTime > 0}; it checks every such entry once per tick and self-cancels
 * once no remaining entry needs expiry-checking.
 *
 * @param <K> the key type identifying a tracked effect instance (e.g. {@code Block}, {@code Player})
 * @param <D> the display type driven by this registry's effects
 */
@ApiStatus.Internal
public abstract class TimedEffectRegistry<K, D extends IColorableDisplay> {

    private final Map<K, Entry> entries = new HashMap<>();
    private BukkitTask lifetimeCheckTask;

    /** Effect-specific teardown beyond the generic bookkeeping already handled by {@link #remove}. */
    protected abstract void onRemove(K key, D display);

    /**
     * Initial value of the per-entry "ticks since last animation" counter when a new entry's tick
     * task starts. Defaults to {@code 0}, meaning a repeating function's first call happens a full
     * {@code animationDuration} ticks after registration (this was {@link
     * me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.ViewTinter ViewTinter}'s original
     * behaviour). {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.BlockHighlighter
     * BlockHighlighter} instead primes this to {@code animationDuration} so its driving function
     * fires on the very first tick after registration - override this method to preserve that.
     */
    protected int initialTicksSinceLastAnimation(int animationDuration) {
        return 0;
    }

    /**
     * Registers a new timed effect under {@code key}, replacing any previous bookkeeping under the
     * same key (callers are expected to have already torn down an old entry via {@link #remove}
     * first, as {@code BlockHighlighter}/{@code ViewTinter} do). Starts the entry's own tick task,
     * and lazily starts the shared lifetime-expiry checker if {@code lifeTime > 0}.
     *
     * @param animationDuration ticks between calls to {@code function}, for repeating functions
     * @param function          may be {@code null} for a static (non-animated) effect
     * @param lifeTime          ticks until this entry is automatically removed; {@code <= 0} disables auto-removal
     */
    public void register(K key, int animationDuration, D display, EffectFunction<D> function, int lifeTime) {
        Entry entry = new Entry(animationDuration, display, function, lifeTime);
        entries.put(key, entry);
        entry.start();
        if (lifeTime > 0) {
            startLifetimeChecker();
        }
    }

    /** Removes {@code key}'s entry, if any: stops its tick task, cancels any colour animation, and runs {@link #onRemove}. */
    public void remove(K key) {
        Entry entry = entries.get(key);
        if (entry == null) return;

        entry.stop();
        GlobalAnimationTickHandler.removeColorAnimation(entry.display);
        onRemove(key, entry.display);
        entries.remove(key);
    }

    /** Removes every currently registered entry. */
    public void removeAll() {
        for (K key : new ArrayList<>(entries.keySet())) {
            remove(key);
        }
    }

    /** The display currently registered under {@code key}, or {@code null} if there is none. */
    public D getDisplay(K key) {
        Entry entry = entries.get(key);
        return entry != null ? entry.display : null;
    }

    /** Whether {@code key} currently has an active entry. */
    public boolean isActive(K key) {
        return entries.containsKey(key);
    }

    /**
     * Swaps the active {@link EffectFunction} on an already-registered key without removing and
     * recreating its display. No-ops if {@code key} isn't currently registered.
     */
    public void setAnimation(K key, EffectFunction<D> function, int animationDuration) {
        Entry entry = entries.get(key);
        if (entry == null) return;

        GlobalAnimationTickHandler.removeColorAnimation(entry.display);
        entry.stop();

        entry.function = function;
        entry.animationDuration = animationDuration;
        entry.transitionAnimationPlayed = false;

        entry.start();
    }

    /**
     * Lazily starts the single shared repeating task that expires entries whose {@code lifeTime}
     * has elapsed. No-ops if already running; cancels itself once no remaining entry has
     * {@code lifeTime > 0}.
     */
    private void startLifetimeChecker() {
        if (lifetimeCheckTask != null) return;

        lifetimeCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                List<K> keysToRemove = new ArrayList<>();
                boolean anyTimed = false;
                for (Map.Entry<K, Entry> mapEntry : entries.entrySet()) {
                    Entry entry = mapEntry.getValue();
                    if (entry.lifeTime > 0) {
                        anyTimed = true;
                        if (entry.tickCounter >= entry.lifeTime) {
                            keysToRemove.add(mapEntry.getKey());
                        }
                    }
                }
                for (K key : keysToRemove) {
                    remove(key);
                }
                if (!anyTimed) {
                    cancel();
                    lifetimeCheckTask = null;
                }
            }
        }.runTaskTimer(PluginHolder.getPlugin(), 0, 1);
    }

    /** Per-key timed-effect state: display, driving function, animation cadence, tick task, lifetime bookkeeping. */
    private final class Entry {
        private final int lifeTime;
        private int animationDuration;
        private int tickCounter = 0;
        private final D display;
        private EffectFunction<D> function;
        private BukkitTask task;
        private boolean transitionAnimationPlayed = false;

        Entry(int animationDuration, D display, EffectFunction<D> function, int lifeTime) {
            this.animationDuration = animationDuration;
            this.display = display;
            this.function = function;
            this.lifeTime = lifeTime;
        }

        /**
         * Starts the repeating tick task that drives {@code function}, honouring
         * {@link EffectFunction#isRepeating()}. No-ops if there is no function to run, or if already started.
         */
        void start() {
            if (task != null) return;
            if (function == null && animationDuration > 0) return;

            task = new BukkitRunnable() {
                int ticksSinceLastAnimation = initialTicksSinceLastAnimation(animationDuration);

                @Override
                public void run() {
                    tickCounter++;
                    ticksSinceLastAnimation++;

                    if (function != null) {
                        if (function.isRepeating()) {
                            if (ticksSinceLastAnimation >= animationDuration) {
                                function.onAnimationRestart(display);
                                ticksSinceLastAnimation = 0;
                            }
                        } else if (!transitionAnimationPlayed) {
                            function.onAnimationRestart(display);
                            transitionAnimationPlayed = true;
                        }
                    }
                }
            }.runTaskTimer(PluginHolder.getPlugin(), 0, 1);
        }

        /** Cancels the repeating tick task, if running. */
        void stop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }
}
