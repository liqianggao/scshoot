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

//        if (java.awt.SystemTray.isSupported) {
//            val toolkit   = awt.Toolkit.getDefaultToolkit
//            val tray      = awt.SystemTray.getSystemTray
//            val traySize  = tray.getTrayIconSize
//            val trayImage = toolkit.getImage(Config.resDir + "tray.png").getScaledInstance(traySize.width, traySize.height, awt.Image.SCALE_SMOOTH)
//            
//            val trayIcon = new java.awt.TrayIcon(trayImage, "Scshoot"){
//                addMouseListener( new java.awt.event.MouseAdapter {
//                    override def mouseClicked( e:java.awt.event.MouseEvent ) {
//                        println("OOOK")
//                        if (e.getButton == java.awt.event.MouseEvent.BUTTON1) {
//                        }
//                    }
//                })
//                
////                addActionListener( new awt.event.ActionListener {
////                    override def actionPerformed(e:awt.event.ActionEvent) {
////                        println(333)
////                    }
////                })
//            }
//            
//            tray.add( trayIcon )
//        }
//    }
//    
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
