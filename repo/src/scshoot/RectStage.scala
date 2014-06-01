package scshoot

class RectStage extends javafx.stage.Stage with ul.GetTextable {
    val rectStage = this
    
    initStyle(javafx.stage.StageStyle.UNDECORATED)
    initModality(javafx.stage.Modality.APPLICATION_MODAL)
    setResizable(false)
    setFullScreen(true)
    setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH)
//    setFullScreenExitHint("")
    
    val selRect = new javafx.scene.shape.Rectangle(Config.shotX, Config.shotY, Config.shotW, Config.shotH) {
        setStroke(javafx.scene.paint.Color.RED)
        setStrokeWidth(2)
        setStrokeLineCap(javafx.scene.shape.StrokeLineCap.SQUARE)
        setFill(javafx.scene.paint.Color.TRANSPARENT)
    }
//    javafx.scene.layout.StackPane.setAlignment(selRect, javafx.geometry.Pos.)
    val image = new javafx.scene.image.WritableImage(Config.screenSize.width, Config.screenSize.height)
    val imageView = new javafx.scene.image.ImageView(image)
    
    object drag{
        var started = false
        var (x,y, rectX,rectY) = (0.0, 0.0, 0.0, 0.0)
        var cursor = javafx.scene.Cursor.DEFAULT
    }
    object select{
        var started = false
        var (fromX, fromY, toX, toY) = (0.0,0.0,0.0,0.0)
        var cursor = javafx.scene.Cursor.DEFAULT
    }
    addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, new javafx.event.EventHandler[javafx.scene.input.MouseEvent]{
        def handle(e:javafx.scene.input.MouseEvent){
            if (e.getButton == javafx.scene.input.MouseButton.PRIMARY){
                if (!drag.started){
                    val x = e.getScreenX
                    val y = e.getScreenY
                    if ((x >= selRect.getX && (x < (selRect.getX + selRect.getWidth))) &&
                        (y >= selRect.getY && (y < (selRect.getY + selRect.getHeight)))){
                        drag.x = x; drag.y = y
                        drag.rectX = selRect.getX; drag.rectY = selRect.getY
                        drag.started = true
                        
                        drag.cursor = rectStage.getScene.getCursor
                        rectStage.getScene.setCursor(javafx.scene.Cursor.MOVE)

                        println("drag started at " + drag.x + "," + drag.y)
                    }
                }
                e.consume
            }
            else if (e.getButton == javafx.scene.input.MouseButton.SECONDARY){
                if (!select.started){
                    select.fromX = e.getScreenX
                    select.fromY = e.getScreenY
                    select.started = true
                    
                    select.cursor = rectStage.getScene.getCursor
                    rectStage.getScene.setCursor(javafx.scene.Cursor.CROSSHAIR)

                    println("selection started at " + select.fromX + "," + select.fromY)
                }
                e.consume
            }
        }
    })
    addEventHandler(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, new javafx.event.EventHandler[javafx.scene.input.MouseEvent]{
        override def handle(e:javafx.scene.input.MouseEvent){
            if (e.getButton == javafx.scene.input.MouseButton.PRIMARY){
                if (drag.started){
                    val dx = e.getScreenX - drag.x
                    val dy = e.getScreenY - drag.y
                    selRect.setX( drag.rectX + dx)
                    selRect.setY( drag.rectY + dy)
                    Config.shotX = (drag.x + dx).toInt
                    Config.shotY = (drag.y + dy).toInt
                }
                e.consume
            }
            else if (e.getButton == javafx.scene.input.MouseButton.SECONDARY){
                if (select.started){
                    select.toX = e.getScreenX
                    select.toY = e.getScreenY
                    
                    selRect.setX(Math.min(select.fromX, select.toX))
                    selRect.setWidth(Math.abs(select.fromX - select.toX))
                    selRect.setY(Math.min(select.fromY, select.toY))
                    selRect.setHeight(Math.abs(select.fromY - select.toY))
                }
                e.consume
            }
        }
    })
    addEventHandler(javafx.scene.input.MouseEvent.MOUSE_RELEASED, new javafx.event.EventHandler[javafx.scene.input.MouseEvent]{
        override def handle(e:javafx.scene.input.MouseEvent){
            if (e.getButton == javafx.scene.input.MouseButton.PRIMARY){
                if (drag.started){
                    drag.started = false
                    rectStage.getScene.setCursor(drag.cursor)
                }
                e.consume
            }
            else if (e.getButton == javafx.scene.input.MouseButton.SECONDARY){
                if (select.started){
                    select.started = false
                    rectStage.getScene.setCursor(select.cursor)
                    
                    Config.shotX = Math.min(select.fromX, select.toX).toInt
                    Config.shotW = Math.abs(select.fromX - select.toX).toInt
                    Config.shotY = Math.min(select.fromY, select.toY).toInt
                    Config.shotH = Math.abs(select.fromY - select.toY).toInt
                }
                e.consume
            }
        }
    })

    addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, new javafx.event.EventHandler[javafx.scene.input.KeyEvent]{
        def handle(e:javafx.scene.input.KeyEvent){
            if (e.getCode == javafx.scene.input.KeyCode.ESCAPE){
                rectStage.close
            }
        }
    })

    setScene(new javafx.scene.Scene( new javafx.scene.layout.Pane {
        getChildren.addAll(
            imageView,
            selRect
        )
    }))
}
