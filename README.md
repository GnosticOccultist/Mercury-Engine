<img src="https://raw.githubusercontent.com/GnosticOccultist/Mercury-Engine/master/docs/test-logo.png" alt="Mercury-Engine" height="130px">

Mercury-Engine is a 3D game engine, which is still in very early development. It is designed to implement
the bedrock of any game or application for you (graphics, assets, scenegraph,...), while still being 
highly flexible for expert programmers.

The engine uses OpenGL as the graphics API, but others may come in the future!

## Features

Side-Note: Most of the features aren't implemented yet or are still in development stage...

- __OpenGL Objects Wrapper__:

    Each OpenGL objects are designed inside a wrapper class, which provides helpful function
to upload, create, store or cleanup data. You can use them to create your own rendering process, without messing
with the very low-level of graphics API. (Note: Not every OpenGL objects are implemented yet!)

- __Scenegraph__:

	Visual and spatial data is represented by a tree containing multiple nodes. Each node must have one parent except for the root node and
depending on implementation some children. 
* 'Anima Mundi': Generic an abstract class for each node of a scenegraph.
* 'Nucleus Mundi': Implementation of 'Anima Mundi' which can have one or multiple children.
* 'Physica Mundi': Implementation of 'Anima Mundi' which represents a visual data that can be rendered. It must define a Mesh and a Material in order to be rendered.

	The Mercury-Engine scenegraph handles traversal techniques named as 'Visitor' to apply some operation(s) to a starting node and recursively moving down the tree to apply
the operation(s) to the entire hierarchy. This technique is used to maintain world/global transformation of a scenegraph, recursively applying the parent transformation to the child's one.

	More informations can be found on scenegraph here: https://en.wikipedia.org/wiki/Scene_graph
