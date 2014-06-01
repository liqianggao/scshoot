package ul

trait StrAttrs {

    def strKeys:Set[String] = Set.empty[String]
    def strSet(k:String, v:String):Unit
    def strGet(k:String):String
    def strSetAll(s:String, sepAttr:String=";", sepVal:String="="):Unit = {
        for (kv <- s.split(sepAttr)) {
            val kvs = kv.split(sepVal)
            if (kvs.length == 2) strSet(kvs(0), kvs(1))
        }
    }
    def strGetAll(sepAttr:String=";", sepVal:String="="):String = {
        (for (k <- strKeys) yield k + sepVal + strGet(k) ).mkString(sepAttr)
    }

}
