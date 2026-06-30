## Animation
When making an animation I need to interpolate between two points. My goal is to
make it posible to start an animation while another is running. The animations
should behave additively. So when an animation is started with the movement of
5,0,1 and duration 100 and another is started at tick 50 of the first one with
-1,1,2 the movement should change direction to move in the addative direction
of both interpolated movements.<br>
I need to watch out for absolute animations that request the position to move to
a specific location in the relative position to the origin. I will have to look
out that I don't take the current animation position but the end position of all
animations combined.<br>
Of course I could also create a seperate animation type that always ends in a
absolute position.