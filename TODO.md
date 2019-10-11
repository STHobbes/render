# TODO:

## Class Website:
* **Refs and Links** - add links (amazon or other) for all of the references.
* **Syllabus** - add some lecture notes.
* **Student Work Pages** - port these
* **Student Projects** - port these

## Ray Trace Code:
* **Functionality**
 * (in progress)Add clipping planes to quadric objects - sphere, ellipsoid, cyslinder, and cone are complete
 * Finish porting all geometry from the old code:
    * blobbies
    * lens
    * group
    * moving object (to support motion blur)
* **Documentation**
  * (done) cleanup to remove all the javadocs generation errors.
  * (in progress) cleanup the documentation of XML format.
  * (in progress) Add missing documentation to the texture abstract classes - Also, look for texture classes that should extend
    the abstract classes and convert them to using the base
  * (done) Document cone and other quadrics, add test environments.
  **All code**
  * (in progress) Analysis and correction
    * (in progress) Reformatting
    * (in progress) Try to reduce duplicate code - there will be some in the assignment code as each assignment
       builds from the previous.
* **high level ray-tracing**
  * (done, needs better documentation) make sure oversampling and distributed ray-tracing implementations are included and working.
* **Textures**
  * (in progress) Test textures with quadrics
  * (in progress) Update textures to use base classes (greatly simplifies them)
  * (in progress) Update textures to be true 3D textures where appropriate.
