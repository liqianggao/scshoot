package scshoot

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
            val robot      = new java.awt.Robot
            
            //file name generator...
            val dateFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm.ss")

            //"save to directory" dialog...
            val saveTo = new javafx.stage.DirectoryChooser
            saveTo.setInitialDirectory( new java.io.File( Config.saveTo ))
            saveTo.setTitle( tr("Save to...") )
            
            //menu items that need to be hidden
            object capture {
                val menuDisable = new collection.mutable.ArrayBuffer[javafx.scene.control.MenuItem]
                val menuEnable  = new collection.mutable.ArrayBuffer[javafx.scene.control.MenuItem]
                
                var rect:java.awt.Rectangle          = null
                var timer:java.util.Timer            = null
                var file:java.io.File                = null
                var apng:ch.reto_hoehener.japng.Apng = null
            }
        }

        //application popup menu...
        val contextMenu:javafx.scene.control.ContextMenu = new javafx.scene.control.ContextMenu {
            val _contextMenu = this
            
            getItems.addAll(
                
                //target directory selection...
                new javafx.scene.control.MenuItem(tr("Save to ... ") + Config.saveTo){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            shot.saveTo.showDialog(_node.getScene.getWindow) match {
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
                ,new javafx.scene.control.MenuItem(tr("Select bounds (ESC - exit)...")){
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

                ,new javafx.scene.control.SeparatorMenuItem

                //get screenshot
                ,new javafx.scene.control.MenuItem(tr("Shot...")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            println("shot...")
                            javafx.application.Platform.runLater( new java.lang.Runnable {
                                def run = {
                                    //hide menu and application window
                                    _contextMenu.hide
                                    Config.primaryStage.setOpacity(0)
                                    Config.primaryStage.toBack
                                    
                                    (new java.util.Timer).schedule(new java.util.TimerTask {
                                        def run = {
                                            val rect = if (Config.shotFullScreen) shot.screenRect
                                                else new java.awt.Rectangle(Config.shotX, Config.shotY, Config.shotW, Config.shotH)
                                            val img = shot.robot.createScreenCapture(rect)
                                            javax.imageio.ImageIO.write(img, "png", new java.io.File( Config.saveTo + java.io.File.separator +
                                                java.time.LocalDateTime.now.format(shot.dateFormat) + ".png"
                                            ))
                                            
                                            
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
                ,new javafx.scene.control.MenuItem(tr("Start capture APNG...")){
                    val _mi = this
                    shot.capture.menuDisable += this

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            println("capture start...")
                            javafx.application.Platform.runLater( new java.lang.Runnable {
                                def run = {
                                    //hide menu and application window
                                    for (m <- shot.capture.menuDisable) m.setDisable(true)
                                    for (m <- shot.capture.menuEnable) m.setDisable(false)
                                    
                                    _contextMenu.hide
                                    Config.primaryStage.toBack
                                    
                                    shot.capture.apng = ch.reto_hoehener.japng.ApngFactory.createApng
                                    shot.capture.apng.setPlayCount(1)
                                    shot.capture.apng.setSkipFirstFrame(false)
                                        
                                    shot.capture.file = new java.io.File(Config.saveTo + java.io.File.separator + java.time.LocalDateTime.now.format(shot.dateFormat) + ".apng")
                                    shot.capture.rect = if (Config.shotFullScreen) shot.screenRect
                                        else new java.awt.Rectangle(Config.shotX, Config.shotY, Config.shotW, Config.shotH)
                                    
                                    shot.capture.timer = new java.util.Timer
                                    shot.capture.timer.scheduleAtFixedRate(new java.util.TimerTask {
                                        def run = {
                                            shot.capture.apng.addFrame(shot.robot.createScreenCapture(shot.capture.rect), 1000)
                                        }
                                    }, 100, 500)
                                }
                            })
                        }
                    })
                }
                //stop APNG capture
                ,new javafx.scene.control.MenuItem(tr("Stop capture...")){
                    shot.capture.menuEnable += this
                    setDisable(true)

                    setOnAction(new javafx.event.EventHandler[javafx.event.ActionEvent]{
                        override def handle(e:javafx.event.ActionEvent) = {
                            println("capture stop...")
                            javafx.application.Platform.runLater( new java.lang.Runnable {
                                def run = {
                                    //stop timer and finalize APNG
                                    shot.capture.timer.cancel
                                    shot.capture.apng.assemble(shot.capture.file)
                                    
                                    //hide menu and application window
                                    for (m <- shot.capture.menuDisable) m.setDisable(false)
                                    for (m <- shot.capture.menuEnable) m.setDisable(true)
                                    
                                    _contextMenu.hide
                                    Config.primaryStage.toFront
                                }
                            })
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
