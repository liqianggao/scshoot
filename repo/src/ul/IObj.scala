package ul

import collection.mutable.{ListBuffer, ArrayBuffer, HashMap, Queue}
import xml.{Node}


/** Basic class representing introspectable attribute */
class IAttr(
        var __tag: String,
        var __name: String  = "",
        var __descr: String = "",
        var __value: Any    = "",
        var __values: Seq[Any] = Nil,
        var __show: Boolean = true,
        var __ro: Boolean   = false,
        var __fix: Boolean  = false,
        var __opts: HashMap[String,Any] = new HashMap[String,Any],
        var __units: String = "",
        var __min: Option[Any] = None,
        var __max: Option[Any] = None,
        var __regex: Option[String] = None,
        var __getCb: ArrayBuffer[(IAttr)=>Unit]     =
            new ArrayBuffer[(IAttr)=>Unit],
        var __setCb: ArrayBuffer[(IAttr,Any)=>Unit] =
            new ArrayBuffer[(IAttr,Any)=>Unit],
        var __descrs:Seq[String] = Nil
    ) {
 
    def copy: IAttr = new IAttr(
        __tag, __name, __descr, __value, __values,
        __show, __ro, __fix,
        new HashMap[String,Any] ++ opts,
        __units, __min, __max, __regex,
        __getCb, __setCb, __descrs)

    def copyTo( a: IAttr ) = {
        if (this.__tag == a.__tag) {
            a.__name   = __name;
            a.__descr  = __descr;
            a.__value  = __value;
            a.__values = __values;
            a.__show   = __show;
            a.__ro     = __ro;
            a.__fix    = __fix;
            a.__opts.clear;
            a.__opts ++= __opts;
            a.__units  = __units;
            a.__min    = __min;
            a.__max    = __max;
            a.__regex  = __regex;
            a.__getCb  = __getCb;
            a.__setCb  = __setCb;
            a.__descrs = __descrs;
        }
    }
    

    def value: Any = {
        if (__getCb != null) {for (cb <- __getCb) cb(this)}
        __value
    }
    def value_=(newValue: Any) = {
        if (__setCb != null) {for (cb <- __setCb) cb(this, newValue)}
        __value = newValue
    }
    def get = value
    def set(newValue: Any) = {value = newValue}
    def i:Int = __value match {
        case v:Int => v
        case v:Long => v.toInt
        case v:Double => v.toInt
        case v:String => v.toInt
        case v:Boolean => if (v) 1 else 0
        case _ => 0
    }
    def i_=(newValue: Int) = { value = newValue }
    def l:Long = __value match {
        case v:Int => v.toLong
        case v:Long => v
        case v:Double => v.toLong
        case v:String => v.toLong
        case v:Boolean => if (v) 1L else 0L
        case _ => 0L
    }
    def l_=(newValue: Long) = { value = newValue }
    def f:Float = __value match {
        case v:Int => v.toFloat
        case v:Long => v.toFloat
        case v:Float => v
        case v:Double => v.toFloat
        case v:String => v.toFloat
        case v:Boolean => if (v) 1.0f else 0.0f
        case _ => 0.0f
    }
    def d_=(newValue: Double) = { value = newValue }
    def d:Double = __value match {
        case v:Int => v.toDouble
        case v:Long => v.toDouble
        case v:Float => v.toDouble
        case v:Double => v
        case v:String => v.toDouble
        case v:Boolean => if (v) 1.0 else 0.0
        case _ => 0.0
    }
    def f_=(newValue: Double) = { value = newValue }
    def s:String = if (__value.isInstanceOf[String]) __value.asInstanceOf[String] else __value.toString
    def s_=(newValue: String) = { value = newValue }
    def b:Boolean = __value match {
        case v:Boolean => v
        case v:Int => v != 0
        case v:Long => v != 0L
        case v:Double => v != 0.0
        case v:String => v.toBoolean
        case _ => false
    }
    def b_=(newValue: Boolean) = { value = newValue }
    def o:IObjT = __value.asInstanceOf[IObjT]
    def o_=(newValue: IObjT) = { value = newValue }

    var __valueDef = __value
    def valueDef = this.__valueDef
    def valueDef_=(newValueDef: Any) = { __valueDef = newValueDef }

    def values = this.__values
    def values_=(newValues: Seq[Any]) = { __values = newValues }

    /// string used for attribute type identification
    def typeStr: String = {
        __value match {
            case v:Int     => "int"
            case v:Long    => "long"
            case v:Double  => "float"
            case v:String  => "str"
            case v:Boolean => "bool"
            case v:IObjT   => "iobj"
            case null      => "null"
            case _         => "none"
        }
    }
    
    def tag = __tag
    def tag_=(newTag: String) = { __tag = newTag }
    
    def name = __name
    def name_=(newName: String) = { __name = newName }
    
    def descr = __descr
    def descr_=(newDescr: String) = { __descr = newDescr }
    
    def descrs = __descrs;
    def descrs_=(newDescrs:Seq[String]) = { __descrs = newDescrs }

    def show = __show
    def show_=(newShow: Boolean) = { __show = newShow }

    def ro = __ro
    def ro_=(newRO: Boolean) = { __ro = newRO }

    def fix = __fix
    def fix_=(newFix: Boolean) = { __fix = newFix }

    def opts = __opts
    def opts_=(newOpts: HashMap[String, Any]) = { __opts = newOpts }
    
    def units = __units
    def units_=(newUnits: String) = { __units = newUnits }

    def min = __min
    def min_=(newValue: Any) = {
        __min = newValue match {
            case null => None
            //case v:Option[Any] => v
            case v => Option(v)
        }
    }
    def max = __max
    def max_=(newValue: Any) = {
        __max = newValue match {
            case null => None
            //case v:Option[Any] => v
            case v => Option(v)
        }
    }

    def regex = __regex
    def regex_=(newValue: Any) = {
        __regex = newValue match {
            case null => None
            //case v:Option[String] => v
            case v:String => Option(v)
            case v => Option(v.toString)
        }
    }

    def getCb: ArrayBuffer[(IAttr)=>Unit] = __getCb
    def getCb_= (newGetCb: ArrayBuffer[(IAttr)=>Unit]) = { __getCb = newGetCb }
    def setCb: ArrayBuffer[(IAttr,Any)=>Unit] = __setCb
    def setCb_= (newSetCb: ArrayBuffer[(IAttr,Any)=>Unit]) = { __setCb = newSetCb }

    override def toString = value.toString
    def fromString( newVal: String ): Boolean = { // parse input string into attribute value
        var result = false
        __value match {
            case v:Int     =>
                try { value = newVal.toInt; result = true }
                catch { case _:Throwable => }
            case v:Long    =>
                try { value = newVal.toLong; result = true }
                catch { case _:Throwable => }
            case v:Double  => 
                try { value = newVal.toDouble; result = true }
                catch { case _:Throwable => }
            case v:String  => value = newVal; result = true
            case v:Boolean => 
                try { value = newVal.toBoolean; result = true }
                catch { case _:Throwable => }
            case v:IObjT   => false
            case null      => false
            case _         => false
        }
        return result
    }

    
}


/// Trait representing object with attributes
trait IObjT {
    
    val __attrs = new ArrayBuffer[IAttr]
    val __tag: String = ""
    val __attrsIdx = HashMap[String, Int]()
    
    attrsReindex
    
    def attrsReindex = {
        __attrsIdx.clear()
        for (i <- 0 to __attrs.length-1) {
            __attrsIdx(__attrs(i).tag) = i
        }
    }
    def attrAdd(newAttr: IAttr, reindex: Boolean=false) = { __attrs += newAttr; if (reindex) attrsReindex }
    def attrsAdd(newAttrs:Seq[IAttr]):IObjT = { __attrs ++= newAttrs; attrsReindex; this }
    def attrsAdd(newAttrs:IObjT):IObjT = { __attrs ++= newAttrs.attrs; attrsReindex; this }
    def attrs = __attrs
    def attrs_= (newAttrs: ArrayBuffer[IAttr]) = { __attrs.clear(); __attrs ++= newAttrs }
    def attrsNum = __attrs.length
    def attrsTags: Seq[String] = for (a <- __attrs) yield a.tag
    def attrsHaveTag( tag: String): Boolean = __attrsIdx.contains(tag)

    def attr( tag: String ): IAttr = {
        if ( attrsHaveTag(tag) ) __attrs( attrIndex(tag) )
        else null
    }
    def attrVal( tag: String ): Any = {
        attrIndex(tag) match {
            case -1 => null
            case i => __attrs( i ).value
        }
    }
    def attrIndex( tag: String ): Int = {
        if ( attrsHaveTag(tag) ) __attrsIdx(tag)
        else -1
    }
    def attrDel( tag: String ) = {
        if ( attrsHaveTag(tag) ) { __attrs.remove( attrIndex(tag) ); attrsReindex }
    }
    def attrsDel( tags: Seq[String] ) = {
        for (tag <- tags) {
            if ( attrsHaveTag(tag) ) { __attrs.remove( attrIndex(tag) ); attrsReindex }
        }
    }
    
    def attrsToMap: Map[String, Any] = {
        (for (a <- __attrs) yield (a.tag, a.value)).toMap
    }
    def attrsFromMap( map: Map[String, Any] ) = {
        for ((t,v) <- map if attrsHaveTag(t)) attr(t).value = v
    }
    
    def attrsCopy:IObj = {
        val o = new IObj;
        for (a <- attrs) o.attrAdd(a.copy)
        o.attrsReindex
        o
    }
    def attrsFromIObj( o: IObjT ) = {
        for (a <- o.attrs if (attrsHaveTag(a.tag))) a.copyTo(attr(a.tag))
    }
    
    def attrsToConf: String = {
        var conf = new StringBuilder
        for (a <- __attrs) {
            conf ++= a.tag; conf ++= " = "; conf ++= a.toString(); conf ++= "\n"
        }
        return conf.toString
    }
    def attrsFromConf( conf: String ) = {
        for ( s <- conf.split("\n") ) {
            "(\\w+) = (.*)".r.findFirstMatchIn( s ) match {
                case Some(m) =>
                    if ((m.groupCount == 2) && (attrsHaveTag(m.group(1)))) {
                        attr(m.group(1)).fromString(m.group(2))
                    }
                case _ =>
            }
        }
    }

    def attrsToXML: Node = {
        var nodes = new Queue[Node]()
        for (a <- __attrs) {
            nodes += <attr tag={a.tag} value={a.toString} />
        }
        return <iobj tag={__tag}>{nodes}</iobj>
    }
    def attrsFromXML( node:scala.xml.Node ) = {
        node match {
            case <iobj>{attrs @ _*}</iobj> =>
                for (a <- attrs) {
                    if ( attrsHaveTag((a \ "@tag").text) )
                        attr((a \ "@tag").text).fromString((a \ "@value").text)
                }
        }
    }

}


/// Class representing object with attributes
class IObj(__attrsInit:Seq[IAttr] = Nil) extends IObjT {
    
    attrsAdd(__attrsInit);

    /** Get attribute by its tag */
    def apply( tag: String ) = {
        if (attrsHaveTag(tag)) attr(tag)
        else null
    }
    
    /** Set attribute by its tag */
    def update( tag:String, newValue:Any ) = {
        if (attrsHaveTag(tag)) {
            val a = attr(tag);
            a.__value match {
                case v:Boolean =>
                    newValue match {
                        case nv:Boolean => a.__value = nv
                        case nv:Byte => a.__value = nv != 0
                        case nv:Short => a.__value = nv != 0
                        case nv:Int => a.__value = nv != 0
                        case nv:Long => a.__value = nv != 0L
                        case nv:Float => a.__value = nv != 0.0F
                        case nv:Double => a.__value = nv != 0.0
                        case nv:String => a.__value = (nv.toLowerCase == "true") || (nv == "1")
                        case _ =>
                    }
                case v:Byte =>
                    newValue match {
                        case nv:Boolean => a.__value = if (nv) 1 else 0
                        case nv:Byte => a.__value = nv
                        case nv:Short => a.__value = nv
                        case nv:Int => a.__value = nv
                        case nv:Long => a.__value = nv
                        case nv:Float => a.__value = nv
                        case nv:Double => a.__value = nv
                        case nv:String => try { a.__value = java.lang.Byte.parseByte(nv) } catch { case _:Throwable => }
                        case _ =>
                    }
                case v:Short =>
                    newValue match {
                        case nv:Boolean => a.__value = if (nv) 1 else 0
                        case nv:Byte => a.__value = nv
                        case nv:Short => a.__value = nv
                        case nv:Int => a.__value = nv
                        case nv:Long => a.__value = nv
                        case nv:Float => a.__value = nv
                        case nv:Double => a.__value = nv
                        case nv:String => try { a.__value = java.lang.Short.parseShort(nv) } catch { case _:Throwable => }
                        case _ =>
                    }
                case v:Int =>
                    newValue match {
                        case nv:Boolean => a.__value = if (nv) 1 else 0
                        case nv:Byte => a.__value = nv
                        case nv:Short => a.__value = nv
                        case nv:Int => a.__value = nv
                        case nv:Long => a.__value = nv
                        case nv:Float => a.__value = nv
                        case nv:Double => a.__value = nv
                        case nv:String => try { a.__value = java.lang.Integer.parseInt(nv) } catch { case _:Throwable => }
                        case _ =>
                    }
                case v:Long =>
                    newValue match {
                        case nv:Boolean => a.__value = if (nv) 1 else 0
                        case nv:Byte => a.__value = nv
                        case nv:Short => a.__value = nv
                        case nv:Int => a.__value = nv
                        case nv:Long => a.__value = nv
                        case nv:Float => a.__value = nv
                        case nv:Double => a.__value = nv
                        case nv:String => try { a.__value = java.lang.Long.parseLong(nv) } catch { case _:Throwable => }
                        case _ =>
                    }
                case v:Double =>
                    newValue match {
                        case nv:Boolean => a.__value = if (nv) 1 else 0
                        case nv:Byte => a.__value = nv
                        case nv:Short => a.__value = nv
                        case nv:Int => a.__value = nv
                        case nv:Long => a.__value = nv
                        case nv:Float => a.__value = nv
                        case nv:Double => a.__value = nv
                        case nv:String => try { a.__value = java.lang.Double.parseDouble(nv) } catch { case _:Throwable => }
                        case _ =>
                    }
                case v:String =>
                    a.__value = newValue.toString;
                case _ =>
            }
        }
    }
}
