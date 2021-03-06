package scshoot

import scala.collection.JavaConverters._

class MainStage extends javafx.scene.layout.StackPane with ul.GetTextable {
    val mainStage = this
    
    getChildren.add( new javafx.scene.image.ImageView( Config.mainImage ){
        val _node = this
        
        setSmooth(true)
        setFitWidth(Config.mainImageWidth)
        setFitHeight(Config.mainImageHeight)
        
        //window dragging...
        var initialX = 0.0
        var initialY = 0.0
        setOnMousePressed( new javafx.event.EventHandler[javafx.scene.input.MouseEvent]{
            override def handle(me:javafx.scene.input.MouseEvent){
                if (me.getButton == javafx.scene.input.MouseButton.PRIMARY){
                    initialX = me.getSceneX
                    initialY = me.getSceneY
                }
            }
        })
        setOnMouseDragged( new javafx.event.EventHandler[javafx.scene.input.MouseEvent]{
            override def handle(me:javafx.scene.input.MouseEvent){
                if (me.getButton == javafx.scene.input.MouseButton.PRIMARY){
                    _node.getScene.getWindow.setX(me.getScreenX() - initialX)
                    _node.getScene.getWindow.setY(me.getScreenY() - initialY)
                }
            }
        })
        
        //screenshot tools...
        object shot {
            val toolkit    = java.awt.Toolkit.getDefaultToolkit
            val screenRect = new java.awt.Rectangle(0,0, Config.screenSize.width, Config.screenSize.height)
            def curRect    = if (Config.shotFullScreen) screenRect else new java.awt.Rectangle(Config.shotX, Config.shotY, Config.shotW, Config.shotH)
            var rect       = curRect
            val robot      = new java.awt.Robot
            
            //file name generator...
            val dateFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm.ss")
            //generate name based on current date/time
            def curName = java.time.LocalDateTime.now.format(dateFormat)
            //generate full location based on current date/time and extension
            def curLocation(ext:String) = Config.saveTo + java.io.File.separator + curName + ext
            //generate full location file based on current date/time and extension
            def curFile(ext:String) = new java.io.File(curLocation(ext))
            
            //menu items that need to be hidden
            object capture {
                val menuDisable = new collection.mutable.ArrayBuffer[javafx.scene.control.MenuItem]
                val menuEnable  = new collection.mutable.ArrayBuffer[javafx.scene.control.MenuItem]
                
                var timer:java.util.Timer            = null
                var file:java.io.File                = null
                var apng:ch.reto_hoehener.japng.Apng = null
            }
            object video {
                val menuEnable = new collection.mutable.ArrayBuffer[javafx.scene.control.MenuItem]
                
                var file:java.io.File                = null
                
                def processCmd(r:java.awt.Rectangle, vrate:Int, audio:Boolean, fn:String):List[String] = {
                    System.getProperty("os.name").toLowerCase match {
                        case s:String if (s.startsWith("linux")) =>
                            List("avconv") :::
                            (if (!audio) Nil else List("-f","alsa","-ac","2", "-ab","32k", "-ar","22050", "-i","pulse", "-acodec","vorbis")) :::
                            List("-f","x11grab","-r","%d".format(vrate),"-s","%dx%d".format(r.width,r.height),"-i",":0.0+%d,%d".format(r.x,r.y), "-vcodec","ffv1", "-coder","ac", "-threads","0", "-y", "%s".format(fn))
                        case s:String if (s.startsWith("win")) =>
                            List(Config.ffmpegLocation + "\\ffmpeg.exe") :::
                            (if (!audio) Nil else List("-f","dshow", "-i","audio=VB-Audio Point")) :::
                            List("-f","dshow","-r","%d".format(vrate), "-i","video=UScreenCapture", "-vcodec","ffv1", "-coder","ac", "-filter:v","crop=%d:%d:%d:%d".format(r.width,r.height,r.x,r.y)) :::
                            (if (!audio) Nil else List("-strict","-2", "-acodec","vorbis", "-ac","2", "-ab","32k", "-ar","22050")) :::
                            List("-y", "%s".format(fn))
                    }
                }
                var process:java.lang.Process = null
            }
        }
//avconv -f x11grab -r 5 -s 451x447 -i :0.0+675,468 -vcodec ffv1 -coder ac -threads 1 -y /home/qwer/work/projects/scala/scshoot/img/2014.06.05-08.45.55.mkv
        //application popup menu...
        val contextMenu:javafx.scene.control.ContextMenu = new javafx.scene.control.ContextMenu {
            val _contextMenu = this
            
            getItems.addAll(
                
                //target directory selection...
                new javafx.scene.control.MenuItem(tr("Save to ... ") + Config.saveTo){
                    val _mi = this
                    shot.capture.menuDisable += this

                    //"save to directory" dialog...
                    val _saveTo = new javafx.stage.DirectoryChooser
                    _saveTo.setInitialDirectory( new java.io.File( Config.saveTo ))
                    _saveTo.setTitle( tr("Save to...") )

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            _saveTo.showDialog(_node.getScene.getWindow) match {
                                case null =>
                                case f:java.io.File =>
                                    Config.saveTo = f.toString
                                    _mi.setText(tr("Save to ... ") + Config.saveTo)
                            }
                        }
                    })
                }

                ,new javafx.scene.control.SeparatorMenuItem

                //capture rectangle selection...
                ,new javafx.scene.control.MenuItem(tr("Bounds (Left-drag, Right-select, ESC - exit)...")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            //hide menu and application window
                            _contextMenu.hide
                            Config.primaryStage.toBack
                            
                            (new java.util.Timer).schedule(new java.util.TimerTask {
                                def run = {
                                    val shotImage = shot.robot.createScreenCapture(shot.screenRect)
                                    javafx.application.Platform.runLater( new java.lang.Runnable {
                                        def run = {
                                            javafx.embed.swing.SwingFXUtils.toFXImage(shotImage, Config.rectStage.image)

                                            Config.rectStage.showAndWait
                                            (new java.util.Timer).schedule(new java.util.TimerTask {
                                                def run = {
                                                    javafx.application.Platform.runLater( new java.lang.Runnable {
                                                        def run = {
                                                            //show menu and application window
                                                            Config.primaryStage.toFront
                                                        }
                                                    })
                                                }
                                            }, 200)
                                        }
                                    })
                                }
                            }, 200)
                        }
                    })
                }
                //capture full screen or not ? ...
                ,new javafx.scene.control.CheckMenuItem(tr("Full screen")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setSelected(Config.shotFullScreen)
                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            Config.shotFullScreen = _mi.isSelected
                        }
                    })
                }

                
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ,new javafx.scene.control.SeparatorMenuItem
                //get screenshot
                ,new javafx.scene.control.MenuItem(tr("Shot...")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
//                            println("shot...")
                            javafx.application.Platform.runLater( new java.lang.Runnable {
                                def run = {
                                    //hide menu and application window
                                    _contextMenu.hide
                                    Config.primaryStage.setOpacity(0)
                                    Config.primaryStage.toBack
                                    
                                    (new java.util.Timer).schedule(new java.util.TimerTask {
                                        def run = {
                                            val img = shot.robot.createScreenCapture(shot.curRect)
                                            javax.imageio.ImageIO.write(img, "png", shot.curFile(".png"))
                                            
                                            (new java.util.Timer).schedule(new java.util.TimerTask {
                                                def run = {
                                                    javafx.application.Platform.runLater( new java.lang.Runnable {
                                                        def run = {
                                                            //show menu and application window
                                                            Config.primaryStage.setOpacity(1)
                                                            Config.primaryStage.toFront
                                                        }
                                                    })
                                                }
                                            }, 200)
                                        }
                                    }, 200)
                                }
                            })
                        }
                    })
                }

                
                
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ,new javafx.scene.control.SeparatorMenuItem

                //start APNG capture
                ,new javafx.scene.control.MenuItem(tr("Capture APNG...")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
//                            println("capture start...")
                            Config.apngFirst = null
                            
                            javafx.application.Platform.runLater( new java.lang.Runnable {
                                def run = {
                                    //hide menu and application window
                                    for (m <- shot.capture.menuDisable) m.setDisable(true)
                                    for (m <- shot.capture.menuEnable) m.setDisable(false)
                                    
                                    _contextMenu.hide
                                    Config.primaryStage.toBack
                                    
                                    shot.capture.apng = ch.reto_hoehener.japng.ApngFactory.createApng
                                    shot.capture.apng.setPlayCount(
                                        if (Config.apngLoop) 0 else 1
                                    )
                                    shot.capture.apng.setSkipFirstFrame(false)
                                        
                                    shot.capture.file = shot.curFile(".apng")
                                    shot.rect = shot.curRect
                                    
                                    shot.capture.timer = new java.util.Timer
                                    shot.capture.timer.scheduleAtFixedRate(new java.util.TimerTask {
                                        def run = {
                                            val img = shot.robot.createScreenCapture(shot.rect)
                                            if (Config.apngFirst == null) Config.apngFirst = img
                                            shot.capture.apng.addFrame(img, Config.apngFrameDelay)
                                        }
                                    }, 100, Config.apngFrameDelay)
                                }
                            })
                        }
                    })
                }
                //stop APNG capture
                ,new javafx.scene.control.MenuItem(tr("Stop")){
                    shot.capture.menuEnable += this
                    setDisable(true)

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
//                            println("capture stop...")
                            javafx.application.Platform.runLater( new java.lang.Runnable {
                                def run = {
                                    //stop timer and finalize APNG
                                    shot.capture.timer.cancel
                                    
                                    (new java.util.Timer).schedule(new java.util.TimerTask{
                                        def run = {
                                            if (Config.apngLoop && (Config.apngFirst != null)){
                                                shot.capture.apng.addFrame(Config.apngFirst, Config.apngLoopDelay)
                                            }
                                            shot.capture.apng.assemble(shot.capture.file)
                                        }
                                    }, 100)
                                    
                                    //hide menu and application window
                                    for (m <- shot.capture.menuDisable) m.setDisable(false)
                                    for (m <- shot.capture.menuEnable)  m.setDisable(true)
                                    
                                    _contextMenu.hide
                                    Config.primaryStage.toFront
                                }
                            })
                        }
                    })
                }
                //loop APNG ? ...
                ,new javafx.scene.control.CheckMenuItem(tr("Loop")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setSelected(Config.apngLoop)
                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            Config.apngLoop = _mi.isSelected
                        }
                    })
                }

                
                
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ,new javafx.scene.control.SeparatorMenuItem

                //start avconv capture
                ,new javafx.scene.control.MenuItem(tr("Capture video...")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
//                            println("video capture start...")
                            javafx.application.Platform.runLater( new java.lang.Runnable {
                                def run = {
                                    //hide menu and application window
                                    for (m <- shot.capture.menuDisable) m.setDisable(true)
                                    for (m <- shot.video.menuEnable) m.setDisable(false)
                                    
                                    _contextMenu.hide
                                    Config.primaryStage.toBack
                                    
                                    shot.rect = shot.curRect
                                    
                                    (new java.util.Timer).schedule(new java.util.TimerTask {
                                        def run = {
                                            val pb = new java.lang.ProcessBuilder(shot.video.processCmd(shot.rect, Config.videoRate, Config.videoAudio, shot.curLocation(".mkv")).asJava)
                                            pb.redirectError(new java.io.File(Config.projectDir + "/out.log"))
                                            shot.video.process = pb.start
                                            println("executed: " + pb.command)
                                        }
                                    }, 100)
                                }
                            })
                        }
                    })
                }
                //stop video capture
                ,new javafx.scene.control.MenuItem(tr("Stop")){
                    shot.video.menuEnable += this
                    setDisable(true)

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
//                            println("capture video stop...")
                            javafx.application.Platform.runLater( new java.lang.Runnable {
                                def run = {
                                    
                                    (new java.util.Timer).schedule(new java.util.TimerTask{
                                        def run = {
                                            if ((shot.video.process != null) && shot.video.process.isAlive) {
                                                shot.video.process.destroy
                                                shot.video.process = null
                                            }
                                        }
                                    }, 100)
                                    
                                    //hide menu and application window
                                    for (m <- shot.capture.menuDisable) m.setDisable(false)
                                    for (m <- shot.video.menuEnable)    m.setDisable(true)
                                    
                                    _contextMenu.hide
                                    Config.primaryStage.toFront
                                }
                            })
                        }
                    })
                }
                //capture audio or not ? ...
                ,new javafx.scene.control.CheckMenuItem(tr("Audio")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setSelected(Config.videoAudio)
                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            Config.videoAudio = _mi.isSelected
                        }
                    })
                }
                //ffmpeg directory selection...
                ,new javafx.scene.control.MenuItem(tr("FFmpeg binary location... ") + Config.ffmpegLocation){
                    val _mi = this
                    shot.capture.menuDisable += this

                    val _locDir = new javafx.stage.DirectoryChooser
                    _locDir.setInitialDirectory( new java.io.File( Config.saveTo ))
                    _locDir.setTitle( tr("FFmpeg directory...") )

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            _locDir.showDialog(_node.getScene.getWindow) match {
                                case null =>
                                case f:java.io.File =>
                                    Config.ffmpegLocation = f.toString
                                    _mi.setText(tr("FFmpeg location... ") + Config.ffmpegLocation)
                            }
                        }
                    })
                }

                
                
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ,new javafx.scene.control.SeparatorMenuItem

                //close program...
                ,new javafx.scene.control.MenuItem("Exit"){
                    shot.capture.menuDisable += this

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            Config.app.stop
                        }
                    })
                }
            )
        }

        //show popup menu...
        setOnContextMenuRequested( new javafx.event.EventHandler[javafx.scene.input.ContextMenuEvent]{
            override def handle(e:javafx.scene.input.ContextMenuEvent){
                contextMenu.show(_node, e.getScreenX, e.getScreenY)
            }
        })
    })
}
