package scshoot

import java.{awt}
import scala.language.reflectiveCalls
import scala.collection.JavaConversions

class Scshoot extends javafx.application.Application with ul.GetTextable {
    val _app = this
    
    override def start(stg:javafx.stage.Stage) {
        Config.app = _app
        Config.primaryStage = stg

        Config.load
        ul.GetText.load(Config.localeDir, "messages", "MESSAGES", Config.lang, "en")
        Config.shotLimit

        stg.setResizable(false)
        stg.initStyle(javafx.stage.StageStyle.TRANSPARENT)
//        stg.toFront
        
        stg.setScene(new javafx.scene.Scene(new MainStage) {
            getStylesheets.add(Config.cssFile)
            setFill(javafx.scene.paint.Color.TRANSPARENT)
        })
        stg.setX(Config.x); stg.setY(Config.y)
        stg.sizeToScene
        
        stg.getScene.getWindow.setOnCloseRequest(new javafx.event.EventHandler[javafx.stage.WindowEvent] {
            def handle(e:javafx.stage.WindowEvent) {
            }
        })
        
        Config.rectStage = new RectStage

        stg.show()
    }

    override def stop() {
        Config.x = Config.primaryStage.getX
        Config.y = Config.primaryStage.getY
        
//        Config.lang   = ul.GetText.tran.lang(MainPane.langBox.getValue)
        
        Config.save
        
        System.exit(0)
    }

}

object Scshoot {
    def main(args: Array[String]) {
        javafx.application.Application.launch(classOf[Scshoot], args: _*);
    }
}
