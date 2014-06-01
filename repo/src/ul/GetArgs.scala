package ul

trait GetArgs extends Props {

    //parse array of arguments and place result to props
    def argsParse(args: Array[String], sep:String = "=") {
        for (a <- args) {
            val as = a.split(sep)
            if (as.length <= 2) {
                argFind(as(0)) match {
                    case Some(attr) =>
                        try {
                            props.updateString(attr.tag, if (as.length == 2) as(1) else "true")
                        } catch { case _:Throwable => } 
                    case None =>
                }
            }
        }
    }
    
    //find property with name
    def argFind(name:String, sep:String = ";"):Option[PropAttr] =
        props.attrs.find( _.name.split(sep).contains(name))
    
    //generate help text
    def argsHelp:String = {
        val s = new StringBuilder
        for (p <- props.attrs) {
            s.append(p.name).append(" [").append(props.typeStr(p.tag)).append(" = ").append(props.get(p.tag)).append("] - ").append(p.descr).append("\n")
        }
        s.toString
    }
    
}
