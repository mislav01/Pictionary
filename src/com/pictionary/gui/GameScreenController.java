package com.pictionary.gui;

import com.pictionary.client.Client;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXSlider;
import com.pictionary.model.Player;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javafx.scene.canvas.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GameScreenController implements Initializable {

    private GraphicsContext graphicsContext;
    private Client client = null;
    private ArrayList<String> imgList = new ArrayList<>();
    private TimerThread timerThread;
    private Document doc;
    //XML
    private Element rootElement;

    @FXML
    private JFXButton btnSaveImage;

    @FXML
    private Canvas cvBoard;

    @FXML
    private JFXButton btnExit;

    @FXML
    private JFXButton btnReady;

    @FXML
    private Label lblTime;

    @FXML
    private TextArea txtArea;

    @FXML
    private TextField tfMessage;

    @FXML
    private JFXColorPicker cp;

    @FXML
    private JFXSlider slider;

    @FXML
    private AnchorPane parent;

    @FXML
    private Label lblWord;

    @FXML
    private TableView tblScore;

    @FXML
    private TableColumn columnName, columnScore;

    @FXML
    private JFXButton btnClear;

    @FXML
    private void sendMessageEvent(ActionEvent event) {
        try {
            if (tfMessage.getText().charAt(0) == '!') {
                if (tfMessage.getText().equals("!pause")) {
                    client.sendMsg("pause//");
                    tfMessage.clear();
                } else if (tfMessage.getText().equals("!play")) {
                    client.sendMsg("unpause//");
                    tfMessage.clear();
                } else if (tfMessage.getText().equals("!replay")) {
                    replayFromXml();
                    tfMessage.clear();
                } else {
                    client.sendMsg("gw//" + tfMessage.getText().substring(1).toLowerCase());

                    //XML
                    Element gw = doc.createElement("gw");
                    gw.appendChild(doc.createTextNode("msg//You guessed word: " + tfMessage.getText().substring(1).toLowerCase()));
                    rootElement.appendChild(gw);

                    tfMessage.clear();
                }
            } else {
                client.sendMsg("msg//" + client.getName() + ": " + tfMessage.getText());
                txtArea.appendText("Me: " + tfMessage.getText() + "\n");

                //XML
                Element msg = doc.createElement("msg");
                msg.appendChild(doc.createTextNode("msg//" + client.getName() + ": " + tfMessage.getText()));
                rootElement.appendChild(msg);

                tfMessage.clear();

            }
        } catch (IOException ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void sendReadyEvent(ActionEvent event) {
        try {
            client.sendMsg("ready");
            btnReady.setDisable(true);
        } catch (IOException ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onBtnClearAction(ActionEvent event) {
        graphicsContext.clearRect(0, 0, cvBoard.getWidth(), cvBoard.getHeight());
        imgList.clear();
        //XML
        addButtonToXml(btnClear);
        try {
            client.sendMsg("clr");
        } catch (IOException ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleMousePressedAction(MouseEvent event) {

        if (!(event.getButton() == MouseButton.PRIMARY)) {
            return;
        }

        graphicsContext.setStroke(cp.getValue());
        graphicsContext.setLineWidth(slider.getValue());
        graphicsContext.beginPath();
        graphicsContext.moveTo(event.getX(), event.getY());
        graphicsContext.stroke();

        Double x = event.getX();
        Double y = event.getY();
        String msg = "prsd" + "//" + x + "//" + y + "//" + slider.getValue() + "//" + graphicsContext.getStroke();

        imgList.add(msg);

        //XML
        Element prsd = doc.createElement("prsd");
        prsd.appendChild(doc.createTextNode(msg));
        rootElement.appendChild(prsd);

        try {
            client.sendMsg(msg);
        } catch (IOException ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void handleMouseDraggedAction(MouseEvent event) {

        if (!(event.getButton() == MouseButton.PRIMARY)) {
            return;
        }

        graphicsContext.lineTo(event.getX(), event.getY());
        graphicsContext.stroke();

        Double x = event.getX();
        Double y = event.getY();
        String msg = "drgd" + "//" + x + "//" + y;

        imgList.add(msg);

        //XML
        Element drgd = doc.createElement("drgd");
        drgd.appendChild(doc.createTextNode(msg));
        rootElement.appendChild(drgd);

        try {
            client.sendMsg(msg);
        } catch (IOException ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void btnExitOnAction(ActionEvent event) {
        if (client != null) {
            client.closeConnection();
        }
        Platform.exit();
    }

    @FXML
    private void onBtnSaveAction(ActionEvent event) {
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(
                            "image.ser")))) {
                oos.writeObject(imgList);
            }
        } catch (IOException ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void drawPressed(String x, String y, String lineWidth, String color) {
        graphicsContext.setLineWidth(Double.valueOf(lineWidth));
        graphicsContext.setStroke(Paint.valueOf(color));
        graphicsContext.beginPath();
        graphicsContext.moveTo(Double.valueOf(x), Double.valueOf(y));
        graphicsContext.stroke();
    }

    private void drawDragged(String x, String y) {
        graphicsContext.lineTo(Double.valueOf(x), Double.valueOf(y));
        graphicsContext.stroke();
    }

    public void setChatClient(Client client) throws IOException {
        this.client = client;
        client.sendMsg("name//" + client.getName());
    }

    public void sendMessage(String msg) throws InterruptedException {
        String[] tokens = msg.split("//");
        System.out.println(tokens[0]);
        if ("msg".equals(tokens[0]) && tokens.length == 2) {
            txtArea.appendText(tokens[1] + "\n");
        } else if ("prsd".equals(tokens[0]) && tokens.length == 5) {
            drawPressed(tokens[1], tokens[2], tokens[3], tokens[4]);
            imgList.add(msg);
        } else if ("drgd".equals(tokens[0]) && tokens.length == 3) {
            drawDragged(tokens[1], tokens[2]);
            imgList.add(msg);
        } else if ("clr".equals(tokens[0]) && tokens.length == 1) {
            graphicsContext.clearRect(0, 0, cvBoard.getWidth(), cvBoard.getHeight());
            imgList.clear();
        } else if ("clrChat".equals(tokens[0]) && tokens.length == 1) {
            txtArea.clear();
        } else if ("gw".equals(tokens[0]) && tokens.length == 2) {

            System.out.println(tokens[1].toUpperCase());
            if ("clear".equals(tokens[1])) {
                Platform.runLater(() -> {
                    lblWord.setVisible(false);
                });

            } else {
                Platform.runLater(() -> {
                    lblWord.setText(tokens[1]);
                    lblWord.setVisible(true);
                });
            }

        } else if ("con".equals(tokens[0]) && tokens.length == 3) {
            Player newPlayer = new Player(tokens[1], Integer.parseInt(tokens[2]));
            tblScore.getItems().add(newPlayer);

        } else if ("tblClear".equals(tokens[0]) && tokens.length == 1) {
            if (!tblScore.getItems().isEmpty()) {
                tblScore.getItems().clear();
            }
        } else if ("startTimer".equals(tokens[0]) && tokens.length == 1) {
            if (timerThread == null) {
                timerThread = new TimerThread(lblTime);
                timerThread.setDaemon(true);
                timerThread.start();
                synchronized (lblTime) {
                    lblTime.setTextFill(Paint.valueOf("RED"));
                    //lblTime.notifyAll();
                }
            }

        } else if ("dsblBoard".equals(tokens[0]) && tokens.length == 1) {
            cvBoard.setMouseTransparent(true);
        } else if ("enblBoard".equals(tokens[0]) && tokens.length == 1) {
            cvBoard.setMouseTransparent(false);
        } else if ("dsblClear".equals(tokens[0]) && tokens.length == 1) {
            btnClear.setDisable(true);
        } else if ("enblClear".equals(tokens[0]) && tokens.length == 1) {
            btnClear.setDisable(false);
        } else if ("pauseTimer".equals(tokens[0]) && tokens.length == 1) {
            synchronized (lblTime) {
                lblTime.setTextFill(Paint.valueOf("YELLOW"));
            }
        } else if ("unpauseTimer".equals(tokens[0]) && tokens.length == 1) {
            synchronized (lblTime) {
                lblTime.setTextFill(Paint.valueOf("RED"));
                lblTime.notifyAll();
            }
        } else if ("stopTimer".equals(tokens[0]) && tokens.length == 1) {
            timerThread.terminate();
            //Write XML-----------------------------------------------------------------------------------
            writeContentToXml();
        } else if ("enblReady".equals(tokens[0]) && tokens.length == 1) {
            btnReady.setDisable(false);
        }
    }

    public void setGraphicsContext(ArrayList<String> imageList) {
        for (String msg : imageList) {
            String[] tokens = msg.split("//");

            if ("prsd".equals(tokens[0]) && tokens.length == 5) {
                drawPressed(tokens[1], tokens[2], tokens[3], tokens[4]);
            } else if ("drgd".equals(tokens[0]) && tokens.length == 3) {
                drawDragged(tokens[1], tokens[2]);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        MakeDraggable makeDraggable = new MakeDraggable(parent);
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        graphicsContext = cvBoard.getGraphicsContext2D();
        cp.setValue(Color.BLACK);
        lblTime.setTextFill(Paint.valueOf("YELLOW"));
        createXmlFile();
    }

    private void createXmlFile() {
        try {
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder
                    = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();

            //Create root element
            rootElement
                    = doc.createElement("Pictionary");
            doc.appendChild(rootElement);

        } catch (ParserConfigurationException | DOMException e) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void writeContentToXml() {
        try {
            TransformerFactory transformerFactory
                    = TransformerFactory.newInstance();
            Transformer transformer
                    = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result
                    = new StreamResult(new File("Pictionary-" + client.getName() + ".xml"));
            transformer.transform(source, result);
        } catch (TransformerException e) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private void addButtonToXml(JFXButton btn) {
        Element button = doc.createElement("button");
        button.appendChild(doc.createTextNode(btn.getId()));
        rootElement.appendChild(button);
    }

    private void replayFromXml() {
        Thread replayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document document = db.parse(new File("Pictionary-" + client.getName() + ".xml"));
                    Node rootNode = document.getDocumentElement();

                    NodeList childNodes = rootNode.getChildNodes();

                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Thread.sleep(3);
                        Node childNode = childNodes.item(i);
                        if (childNode.getChildNodes().item(0).getNodeValue().equals("btnClear")) {
                            onBtnClearAction(new ActionEvent());
                        } else {
                            sendMessage(childNode.getChildNodes().item(0).getNodeValue());
                        }
                    }
                } catch (FileNotFoundException | ParserConfigurationException ex) {
                    Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException | IOException | InterruptedException ex) {
                    Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        replayThread.start();
    }
}
