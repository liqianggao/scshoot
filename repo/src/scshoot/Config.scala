package scshoot

import scala.reflect.BeanProperty

object Config extends ul.Props {

    val project    = "scshoot"
    val projectDir = System.getProperty("user.dir") + java.io.File.separator
    
    var app:javafx.application.Application = null
    var primaryStage: javafx.stage.Stage = null
    val screenSize = java.awt.Toolkit.getDefaultToolkit.getScreenSize
    var rectStage:RectStage = null
    
    val configFile = projectDir + project + ".conf"
    
    val localeDir  = projectDir + "locale"

    val cssDir     = projectDir + "css" + java.io.File.separator
    val cssFile    = "file:" + cssDir + project + ".css"
    
    val scriptsDir = projectDir + "scripts" + java.io.File.separator
    
    val resDir = projectDir + "res" + java.io.File.separator

    val mainImage = "file:" + resDir + "main.png"
    val mainImageWidth  = 64
    val mainImageHeight = 64

    @BeanProperty var x        = 10.0
    @BeanProperty var y        = 10.0
    
    @BeanProperty var lang     = "en"

    @BeanProperty var saveTo   = "."

    @BeanProperty var shotX    = 0
    @BeanProperty var shotY    = 0
    @BeanProperty var shotW    = 100
    @BeanProperty var shotH    = 100
    @BeanProperty var shotFullScreen = false

    props.attrs ++= List(
        new ul.PropAttr("x")
        ,new ul.PropAttr("y")
        
        ,new ul.PropAttr("lang")

        ,new ul.PropAttr("saveTo")

        ,new ul.PropAttr("shotX")
        ,new ul.PropAttr("shotY")
        ,new ul.PropAttr("shotW")
        ,new ul.PropAttr("shotH")
        ,new ul.PropAttr("shotFullScreen")
    )
    
    def save = {
        try {
            java.nio.file.Files.write(
                (new java.io.File(configFile)).toPath,
                props.toConf.getBytes("UTF8")//,
//                java.nio.file.StandardOpenOption.CREATE
            )
        } catch { case _:Throwable => }
    }
    
    def load = {
        try {
            props.fromConf( new String(
                java.nio.file.Files.readAllBytes((new java.io.File(configFile)).toPath),
                "UTF8"
            ))
        } catch { case _:Throwable => }
    }

    //limit shot bounds
    def shotLimit = {
        if (shotX < 0) shotX = 0
        if (shotX >= screenSize.width-1) shotX = 0
        if (shotW > screenSize.width) shotW = screenSize.width

        if (shotY < 0) shotY = 0
        if (shotY >= screenSize.height-1) shotY = 0
        if (shotH > screenSize.height) shotH = screenSize.height
    }
}
