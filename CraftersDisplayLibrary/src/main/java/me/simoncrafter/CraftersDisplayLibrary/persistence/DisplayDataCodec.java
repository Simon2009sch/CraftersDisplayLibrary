package me.simoncrafter.CraftersDisplayLibrary.persistence;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Encode/decode helper for the {@code CDL_DISPLAY_DATA}/{@code CDL_ANCESTOR_CHAIN} persistent-data
 * blobs written by {@link DisplayPersistence}.
 * <p>
 * <b>Format.</b> A blob is one {@link String} made of back-to-back length-prefixed fields:
 * {@code <length>:<content>}, where {@code length} is {@code content.length()} - Java's
 * {@code String#length()} (UTF-16 code units), <em>not</em> a UTF-8 byte count. This is a
 * deliberate choice: both {@link Writer} and {@link Reader} operate directly on
 * {@link String}/{@code StringBuilder} (never on raw bytes), so indexing by
 * {@code String#length()}/{@code String#substring} is exact and free of any byte/char boundary
 * mismatch - there is no encoding step where that distinction could matter. A field is located by
 * its declared length rather than by scanning for a delimiter, which is what makes this robust
 * against free-text content (e.g. {@code TextDisplay}'s text) containing any character at all,
 * including {@code ':'} itself. A {@code null} string is written as length {@code -1} with empty
 * content ({@code "-1:"}).
 * <p>
 * Every other field type (ints, floats, booleans, colors, vectors, quaternions, transformations,
 * locations...) is funneled through the same {@code writeString}/{@code readString} primitive via
 * {@code String.valueOf(...)}/parsing, per the design note in the implementation plan: one simple,
 * consistent text format rather than a binary mix. The one exception is raw binary content (
 * currently only {@code ItemStack#serializeAsBytes()}), which is Base64-encoded into a string field
 * first via {@link Writer#writeBytes(byte[])}/{@link Reader#readBytes()}.
 * <p>
 * Not thread-safe; each {@link Writer}/{@link Reader} is meant to be used for exactly one blob.
 */
final class DisplayDataCodec {

    private DisplayDataCodec() {}

    /** Builds one blob by appending fields in a fixed, caller-defined order. */
    static final class Writer {
        private final StringBuilder sb = new StringBuilder();

        Writer writeString(String s) {
            if (s == null) {
                sb.append("-1:");
                return this;
            }
            sb.append(s.length()).append(':').append(s);
            return this;
        }

        Writer writeBoolean(boolean b) {
            return writeString(Boolean.toString(b));
        }

        Writer writeInt(int i) {
            return writeString(Integer.toString(i));
        }

        Writer writeLong(long l) {
            return writeString(Long.toString(l));
        }

        Writer writeFloat(float f) {
            return writeString(Float.toString(f));
        }

        Writer writeDouble(double d) {
            return writeString(Double.toString(d));
        }

        /** Writes an ARGB color, or a null marker if {@code color} is {@code null}. */
        Writer writeColor(Color color) {
            return writeString(color == null ? null : Integer.toString(color.asARGB()));
        }

        Writer writeVector3f(Vector3f v) {
            writeFloat(v.x);
            writeFloat(v.y);
            writeFloat(v.z);
            return this;
        }

        Writer writeQuaternionf(Quaternionf q) {
            writeFloat(q.x);
            writeFloat(q.y);
            writeFloat(q.z);
            writeFloat(q.w);
            return this;
        }

        Writer writeTransformation(Transformation t) {
            writeVector3f(t.getTranslation());
            writeQuaternionf(t.getLeftRotation());
            writeVector3f(t.getScale());
            writeQuaternionf(t.getRightRotation());
            return this;
        }

        /** Writes the world name (or a null marker for a {@code null}/unloaded world) and x/y/z. */
        Writer writeLocation(Location loc) {
            writeString(loc.getWorld() != null ? loc.getWorld().getName() : null);
            writeDouble(loc.getX());
            writeDouble(loc.getY());
            writeDouble(loc.getZ());
            return this;
        }

        /** Base64-encodes {@code bytes} into a string field; writes a null marker if {@code bytes} is {@code null}. */
        Writer writeBytes(byte[] bytes) {
            return writeString(bytes == null ? null : Base64.getEncoder().encodeToString(bytes));
        }

        Writer writeUUID(UUID uuid) {
            return writeString(uuid == null ? null : uuid.toString());
        }

        /** Writes {@code brightness}, or a null marker if it is {@code null} (an unset override). */
        Writer writeBrightness(Display.Brightness brightness) {
            if (brightness == null) {
                writeBoolean(false);
                return this;
            }
            writeBoolean(true);
            writeInt(brightness.getBlockLight());
            writeInt(brightness.getSkyLight());
            return this;
        }

        /** Writes a list of ancestor records (furthest-ancestor-first) as a count followed by that many {@code (uuid, type, data)} triples. */
        Writer writeAncestorChain(List<DisplayPersistence.AncestorRecord> chain) {
            writeInt(chain.size());
            for (DisplayPersistence.AncestorRecord record : chain) {
                writeUUID(record.uuid());
                writeString(record.type());
                writeString(record.blob());
            }
            return this;
        }

        String build() {
            return sb.toString();
        }
    }

    /** Reads fields back out of a blob in the same order they were written. */
    static final class Reader {
        private final String data;
        private int pos = 0;

        Reader(String data) {
            this.data = data == null ? "" : data;
        }

        String readString() {
            int colon = data.indexOf(':', pos);
            int len = Integer.parseInt(data.substring(pos, colon));
            pos = colon + 1;
            if (len < 0) return null;
            String s = data.substring(pos, pos + len);
            pos += len;
            return s;
        }

        boolean readBoolean() {
            return Boolean.parseBoolean(readString());
        }

        int readInt() {
            return Integer.parseInt(readString());
        }

        long readLong() {
            return Long.parseLong(readString());
        }

        float readFloat() {
            return Float.parseFloat(readString());
        }

        double readDouble() {
            return Double.parseDouble(readString());
        }

        Color readColor() {
            String s = readString();
            return s == null ? null : Color.fromARGB(Integer.parseInt(s));
        }

        Vector3f readVector3f() {
            return new Vector3f(readFloat(), readFloat(), readFloat());
        }

        Quaternionf readQuaternionf() {
            return new Quaternionf(readFloat(), readFloat(), readFloat(), readFloat());
        }

        Transformation readTransformation() {
            Vector3f translation = readVector3f();
            Quaternionf leftRotation = readQuaternionf();
            Vector3f scale = readVector3f();
            Quaternionf rightRotation = readQuaternionf();
            return new Transformation(translation, leftRotation, scale, rightRotation);
        }

        /** Reads a location; the world is resolved via {@link Bukkit#getWorld(String)} at read time (so it must already be loaded). */
        Location readLocation() {
            String worldName = readString();
            double x = readDouble();
            double y = readDouble();
            double z = readDouble();
            World world = worldName != null ? Bukkit.getWorld(worldName) : null;
            return new Location(world, x, y, z);
        }

        byte[] readBytes() {
            String b64 = readString();
            return b64 == null ? null : Base64.getDecoder().decode(b64);
        }

        UUID readUUID() {
            String s = readString();
            return s == null ? null : UUID.fromString(s);
        }

        Display.Brightness readBrightness() {
            boolean present = readBoolean();
            if (!present) return null;
            int blockLight = readInt();
            int skyLight = readInt();
            return new Display.Brightness(blockLight, skyLight);
        }

        List<DisplayPersistence.AncestorRecord> readAncestorChain() {
            int count = readInt();
            List<DisplayPersistence.AncestorRecord> chain = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                UUID uuid = readUUID();
                String type = readString();
                String blob = readString();
                chain.add(new DisplayPersistence.AncestorRecord(uuid, type, blob));
            }
            return chain;
        }
    }
}
