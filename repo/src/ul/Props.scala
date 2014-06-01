package ul

import java.net.{URLDecoder,URLEncoder}

trait Props{
    val propsContainer:Props = this
    val props = new PropsMan {
        container = propsContainer
    }
}

class PropsMan{
    val propsMan = this
    
    var container:Props = null;
    val attrs = new collection.mutable.ArrayBuffer[PropAttr]
    
    def add(a:Seq[PropAttr]) = { a.foreach( _.container = propsMan ); attrs ++= a }

    def apply(name:String):Any =
        container.getClass.getMethod("get"+name.capitalize).invoke(container)
    def get(name:String) = apply(name)

    def update(name:String, value:Int):PropsMan = update(name, new java.lang.Integer(value))
    def update(name:String, value:Double):PropsMan = update(name, new java.lang.Double(value))
    def update(name:String, value:Object):PropsMan = {
        container.getClass.getMethods.find(_.getName == "set"+name.capitalize).get.invoke(container, value)
        this
    }
    def updateString(name:String, value:String):PropsMan = {
        update(name, apply(name) match {
            case i:java.lang.Integer => new java.lang.Integer(java.lang.Integer.parseInt(value))
            case d:java.lang.Double  => new java.lang.Double(java.lang.Double.parseDouble(value))
            case b:java.lang.Boolean => new java.lang.Boolean(java.lang.Boolean.parseBoolean(value))
            case s:java.lang.String  => value
            case _                   => value
        })
        this
    }
    def setStr(n:String, v:String) = updateString(n,v)
    
    def tags = (for (a<-attrs) yield a.tag)
    def names = (for (a<-attrs) yield a.name)
    def attr(aTag:String):PropAttr = attrs.find(_.tag == aTag).get
    def typeStr(tag:String):String = apply(tag) match {
        case i:java.lang.Integer => "Integer"
        case d:java.lang.Double  => "Double"
        case b:java.lang.Boolean => "Boolean"
        case s:java.lang.String  => "String"
        case _                   => ""
    }
    
    def toConf:String = (
        for (a <- attrs) yield
            a.tag + " = " + URLEncoder.encode(apply(a.tag).toString, "UTF-8")
        ).mkString("\n")
    def fromConf(s:String) {
        for (l <- s.lines) {
            val snv = l.split(" = ")
            if ((snv.length == 2)&&(tags.contains(snv(0))))
                updateString(snv(0), URLDecoder.decode(snv(1), "UTF-8"))
        }
    }
    
    def toXml:String = {
        ""
    }
    def fromXml(s:String) {
    }
    
    def toJson:String = {
        ""
    }
    def fromJson(s:String) {
    }
}

class PropAttr(
    val tag:String,  //property tag
    val name:String, //property name
    val descr:String //property description
) {
    var container:PropsMan = null
    
    var hide = false
    var ro   = false
    var min = Double.NaN
    var max = Double.NaN
    val vals = new collection.mutable.ArrayBuffer[Any]
    val otherOptions = collection.mutable.Map[String, Any]()

    def this(aTag:String, aName:String) = this(aTag,aName,"")
    def this(aTag:String) = this(aTag,aTag)
    
    def typeStr = container.typeStr(this.tag)
    
    def get = container.get(this.tag)
    def getString = container.get(this.tag).toString
    
    def getAsInteger = container.get(this.tag).asInstanceOf[java.lang.Integer]
    def getAsDouble  = container.get(this.tag).asInstanceOf[java.lang.Double]
    def getAsBoolean = container.get(this.tag).asInstanceOf[java.lang.Boolean]
    def getAsString  = container.get(this.tag).asInstanceOf[java.lang.String]
    
    def set(v:Object) = container.update(this.tag, v)
    def setString(v:String) = container.setStr(this.tag, v)
}
