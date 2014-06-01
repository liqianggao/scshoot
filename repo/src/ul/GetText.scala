package ul

import java.{io, util}
import collection.mutable.{HashMap};

/**
 * Gettext support class
 * @param root translations root directory
 * @param domain translation domain (typically 'messages' or program name)
 * @param category translation category (typically "MESSAGES")
 * @param lang current translation language (default "en")
 * @param langDef original language (default "en")
 */
class GetText(
        var root: String     = "./locale",
        var domain: String   = "messages",
        var category: String = "MESSAGES",
        var lang: String     = "en",
        var langDef: String  = "en"
    ) {
    
    /**
     * Translations catalog map in format [lang => [str => [tran1,...tranN]]]
     */
    val cat = new HashMap[String, HashMap[String, Array[Array[String]]]];
    load; // load/parse catalog
    
    /** Load all translations from root directory */
    def load:GetText = {
        try {
            for (langDir <- new io.File(root).list if (new io.File(root + io.File.separator + langDir).isDirectory)) { //iterate over all dirs in root
                try {
                    println("Loading: " + (root + io.File.separator + langDir + io.File.separator + 
                        "LC_" + category + io.File.separator + domain + ".mo"))
                    //try to load and parse .MO file
                    val moBytes = scala.io.Source.fromFile(
                        root + io.File.separator + langDir + io.File.separator + 
                        "LC_" + category + io.File.separator + domain + ".mo",
                        "ISO-8859-1"
                        ).map(_.toByte ).toArray;
                    val moCat = parseMO( moBytes )
                    cat(langDir) = moCat
                } catch { case _:Throwable => }
            }
        } catch { case _:Throwable => }
        this
    }
    
    /**
     * Parse .MO file contents
     * @param mo file contents in byte array
     * @return translations map
     */
    def parseMO( mo:Array[Byte] ):HashMap[String, Array[Array[String]]] = {
        val lcat = new HashMap[String, Array[Array[String]]];
        if (mo.length > 24) {
            // check magick for msb/lsb
            val magick = (((mo(0) & 0xff) * 256 + (mo(1) & 0xff)) * 256 + (mo(2) & 0xff)) * 256 + (mo(3) & 0xff);
            val msb = magick == 0x950412deL;
            
            if ((magick == 0x950412de) || (magick == 0xde120495)) {
                
                def u32( i:Int ):Int = if (msb)
                    ((((mo(i) & 0xff) * 256 + (mo(i+1) & 0xff)) * 256 + (mo(i+2) & 0xff)) * 256 + (mo(i+3) & 0xff));
                else
                    ((((mo(i+3) & 0xff) * 256 + (mo(i+2) & 0xff)) * 256 + (mo(i+1) & 0xff)) * 256 + (mo(i) & 0xff));
                
                val rev = u32(4); // revision
                val (revMaj, revMin) = (rev >> 16, rev % 65536); // major/minor revision
                // number of strings, offsets of original and translation strings tables
                val (sn, oto, ott) = (u32(8), u32(12), u32(16));

                
                if (sn > 1 && revMaj <= 1 && revMin <= 1) {
                    for (sc <- 1 to sn-1) { // process all strings
                        // original string(s) length, offset
                        val (osl, oso) = ( u32( oto + 8 * sc ), u32( oto + 8 * sc + 4 ) );
                        // translation string(s) length, offset
                        val (tsl, tso) = ( u32( ott + 8 * sc ), u32( ott + 8 * sc + 4 ) );
                        
                        if (osl > 0 && tsl > 0) {
                            // original string(s)
                            val os = mo.slice( oso, oso + osl + 1);
                            // extract all original forms
                            var (oss, ossp) = (List[String](), 0);
                            for (i <- 0 to os.length-1) {
                                if ( os(i) == 0 ) {
                                    oss = oss ::: new String(os.slice(ossp, i), "utf8") :: Nil;
                                    ossp = i + 1;
                                }
                            }
                            // translation string(s)
                            val ts = mo.slice( tso, tso + tsl + 1);
                            // extract all translation forms
                            var (tss, tssp) = (List[String](), 0);
                            for (i <- 0 to ts.length-1) {
                                if ( ts(i) == 0 ) {
                                    tss = tss ::: new String(ts.slice(tssp, i), "utf8") :: Nil;
                                    tssp = i + 1;
                                }
                            }
                            lcat( oss(0) ) = Array( oss.toArray, tss.toArray );
                        }
                    }
                }
                
            }
        }
        return lcat
    }
    
    /**
     * Parse .PO file
     * @param po sequence of strings from file
     * @return translations map
     */
    def parsePo( po:Seq[String] ): HashMap[String, Array[String]] = {
        val lcat = new HashMap[String, Array[String]];
/*        tranMap.clear // clear map
        localeDir = aLocaleDir
        domainName = aDomainName
        // search for translations
        try {
            for (langDir <- new File(localeDir).list()) {
                try {
                    val poFile = new File(localeDir + File.separator + langDir + File.separator + 
                        messagesDir + File.separator + domainName + ".po")
                    val moFile = new File(localeDir + File.separator + langDir + File.separator + 
                        messagesDir + File.separator + domainName + ".mo")
                    if (poFile.exists()) { // parse .po file
                        tranMap(langDir) = new HashMap[String, String]()
                        val fi = new FileInputStream( poFile )
                        val din = new Array[Byte](poFile.length().toInt)
                        fi.read( din ); fi.close()
                        val poStr = new String(din, poEncoding)
                        val poRe = "(?msu:msgid +\"(.*?)\".*?msgstr +\"(.*?)\")".r
                        for (m <- poRe.findAllIn( poStr ).matchData if ((m.groupCount == 2)&&(m.group(1).length > 0)))
                            tranMap(langDir)(m.group(1)) = m.group(2);
                    } else if (moFile.exists()) { // parse .mo file
                        
                    }
                } catch { case _=> if (logger != null) logger.log("Error loading localization directory " + langDir + ".") }
            }
        } catch { case _=> if (logger != null) logger.log("Error loading localization.") }
*/        
        return lcat;
    }
    
    /**
     * Translation function
     * @param s original string
     * @param pl plural form
     * @param aLang translation language
     * @return translated string
     */
    def tr( s: String, pl: Int, aLang: String ): String = {
        try {
            return cat(aLang)(s)(1)(pl-1);
        } catch {
            case _:Throwable => return s;
        }
    }

    /**
     * Translation function
     * @param s original string
     * @param pl plural form
     * @return translated string
     */
    def tr( s: String, pl: Int ): String = { /// translation function
        try {
            return cat(lang)(s)(1)(pl-1);
        } catch {
            case _:Throwable => return s;
        }
    }

    /**
     * Translation function
     * @param s original string
     * @return translated string
     */
    def tr( s: String ): String = { /// translation function
        try {
            return cat(lang)(s)(1)(0);
        } catch {
            case _:Throwable => return s;
        }
    }

    def langs: Seq[String] = langDef :: (for ((l,t) <- cat) yield l).toList;
    def langsNum = langs.length;
    def lang(l:String):String = {
        if (langs.contains(l)) l
        else if (displayLangs.contains(l)) langs(displayLangs.indexOf(l))
        else ""
    }
    def lang(l:Int):String = langs(l);
    def displayLangs:Seq[String] =
        (for (l <- langs) yield {
            val ls = l.split("_");
            val loc = if (ls.length == 1) new util.Locale(l) else new util.Locale(ls(0), ls(1));
            loc.getDisplayName(loc).capitalize;
        });
    def displayLang(l:String):String = {
        if (langs.contains(l)) displayLangs(langs.indexOf(l))
        else if (displayLangs.contains(l)) l
        else ""
    }
    def displayLang(l:Int):String = displayLangs(l);
    def langIndex(l:String):Int = {
        if (langs.contains(l)) langs.indexOf(l)
        else if (displayLangs.contains(l)) displayLangs.indexOf(l)
        else -1
    }
    
}

object GetText {
    var tran:GetText = null;
    
    def tr(s:String):String = {
        if (tran == null) s else tran.tr(s)
    }
    
    def lang = tran.lang
    def lang_=(l:String) = {tran.lang = l}
    def lang_=(l:Int) = {tran.lang = langs(l)}
    def displayLang = displayLangs( if (displayLangs.indexOf(lang) == -1) 0 else displayLangs.indexOf(lang) )
    
    def langs = tran.langs
    def displayLangs = tran.displayLangs
    
    def langIndex(l:String) = tran.langIndex(l)
    
    def load(path:String, domain:String, category:String, lang:String, langDef:String) = {
        tran =  new GetText(path, domain, category, lang, langDef);
    }
    def load(path:String):Unit = {
        load(path, "messages", "MESSAGES", "en", "en")
    }
    def load:Unit = load("./locale");
}

trait GetTextable {
    def tr(s:String) = GetText.tr(s)
}
