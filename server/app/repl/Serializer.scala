package repl

import java.io._

object Serializer {

  val basePath = "app/repl/"

  def serialize(obj: Serializable, fileName: String) = {
    val fos = new FileOutputStream(basePath + fileName)
    val oos = new ObjectOutputStream(fos)
    oos.writeObject(obj)
    oos.close()
  }

  class ObjectInputStreamWithCustomClassLoader(
    fileInputStream: FileInputStream
      ) extends ObjectInputStream(fileInputStream) {
    override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
      try { Class.forName(desc.getName, false, getClass.getClassLoader) }
      catch { case ex: ClassNotFoundException => super.resolveClass(desc) }
    }
  }

  def deserialize(fileName: String): AnyRef = {
    val fis = new FileInputStream(basePath + fileName)
    val ois = new ObjectInputStreamWithCustomClassLoader(fis)

    val ret = ois.readObject()
    ois.close()
    ret
  }
}
