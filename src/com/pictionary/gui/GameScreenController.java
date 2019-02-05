package com.pictionary.gui;

import com.pictionary.client.Client;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXSlider;
import com.pictionary.model.Player;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GameScreenController implements Initializable {

    private GraphicsContext graphicsContext;
    private Client client = null;
    private ArrayList<String> imgList = new ArrayList<>();
    private TimerThread timerThread;
    private Document doc;
    //XML
    private Element buttons;
    private Element drawing;
    private Element mousePressed;
    private Element mouseDragged;
    private Element chat;
    private int xmlOrderAttr = 0;

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
                    //XML
                    Element pause = doc.createElement("pause");
                    Attr attrOrder = doc.createAttribute("order");
                    attrOrder.setValue(Integer.toString(xmlOrderAttr++));
                    pause.setAttributeNode(attrOrder);
                    pause.appendChild(doc.createTextNode("!pause"));
                    chat.appendChild(pause);
                } else if (tfMessage.getText().equals("!play")) {
                    client.sendMsg("unpause//");
                    tfMessage.clear();
                    //XML
                    Element play = doc.createElement("play");
                    Attr attrOrder = doc.createAttribute("order");
                    attrOrder.setValue(Integer.toString(xmlOrderAttr++));
                    play.setAttributeNode(attrOrder);
                    play.appendChild(doc.createTextNode("!play"));
                    chat.appendChild(play);
                } else if (tfMessage.getText().equals("!replay")) {
                    replayFromXml();
                    tfMessage.clear();
                } else {
                    client.sendMsg("gw//" + tfMessage.getText().substring(1).toLowerCase());

                    //XML
                    Element gw = doc.createElement("gw");
                    Attr attrOrder = doc.createAttribute("order");
                    attrOrder.setValue(Integer.toString(xmlOrderAttr++));
                    gw.setAttributeNode(attrOrder);
                    gw.appendChild(doc.createTextNode("gw//" + tfMessage.getText().substring(1).toLowerCase()));
                    chat.appendChild(gw);

                    tfMessage.clear();
                }
            } else {
                client.sendMsg("msg//" + client.getName() + ": " + tfMessage.getText());
                txtArea.appendText("Me: " + tfMessage.getText() + "\n");

                //XML
                Element msg = doc.createElement("msg");
                Attr attrOrder = doc.createAttribute("order");
                attrOrder.setValue(Integer.toString(xmlOrderAttr++));
                msg.setAttributeNode(attrOrder);
                msg.appendChild(doc.createTextNode("msg//" + client.getName() + ": " + tfMessage.getText()));
                chat.appendChild(msg);

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
            //XML
            addButtonToXml(btnReady);
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
        Attr attrOrder = doc.createAttribute("order");
        attrOrder.setValue(Integer.toString(xmlOrderAttr++));
        prsd.setAttributeNode(attrOrder);
        prsd.appendChild(doc.createTextNode(msg));
        mousePressed.appendChild(prsd);

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
        Attr attrOrder = doc.createAttribute("order");
        attrOrder.setValue(Integer.toString(xmlOrderAttr++));
        drgd.setAttributeNode(attrOrder);
        drgd.appendChild(doc.createTextNode(msg));
        mouseDragged.appendChild(drgd);

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
            Element rootElement
                    = doc.createElement("Pictionary");
            doc.appendChild(rootElement);

            //Create buttons element
            buttons
                    = doc.createElement("Buttons");
            rootElement.appendChild(buttons);
            //Create drawing element
            drawing
                    = doc.createElement("Drawing");
            rootElement.appendChild(drawing);

            mousePressed
                    = doc.createElement("mousePressed");
            drawing.appendChild(mousePressed);

            mouseDragged
                    = doc.createElement("mouseDragged");
            drawing.appendChild(mouseDragged);

            //Create chat element
            chat
                    = doc.createElement("Chat");
            rootElement.appendChild(chat);

        } catch (Exception e) {
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
        Attr attrOrder = doc.createAttribute("order");
        attrOrder.setValue(Integer.toString(xmlOrderAttr++));
        button.setAttributeNode(attrOrder);
        button.appendChild(doc.createTextNode(btn.getId()));
        buttons.appendChild(button);
    }

    private void replayFromXml() {
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream("Pictionary-" + client.getName() + ".xml"));

            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();

                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    if (startElement.getName().getLocalPart().equals("button")) {
                        xmlEvent = xmlEventReader.nextEvent();
                        String buttonId = xmlEvent.asCharacters().getData();
                        System.out.println(buttonId);
                    } else if (startElement.getName().getLocalPart().equals("mousePressed")) {
                        xmlEvent = xmlEventReader.nextEvent();
                        String msg = "";
                        if (!xmlEvent.asCharacters().getData().isEmpty()) {
                            msg = xmlEvent.asCharacters().getData();
                        }
                        System.out.println(msg);
                    } else if (startElement.getName().getLocalPart().equals("mouseDragged")) {
                        xmlEvent = xmlEventReader.nextEvent();
                        String msg = "";
                        if (!xmlEvent.asCharacters().getData().isEmpty()) {
                            msg = xmlEvent.asCharacters().getData();
                        }
                        System.out.println(msg);
                    } else if (startElement.getName().getLocalPart().equals("play")) {
                        xmlEvent = xmlEventReader.nextEvent();
                        String msg = "";
                        if (!xmlEvent.asCharacters().getData().isEmpty()) {
                            msg = xmlEvent.asCharacters().getData();
                        }
                        System.out.println(msg);
                    } else if (startElement.getName().getLocalPart().equals("pause")) {
                        xmlEvent = xmlEventReader.nextEvent();
                        String msg = "";
                        if (!xmlEvent.asCharacters().getData().isEmpty()) {
                            msg = xmlEvent.asCharacters().getData();
                        }
                        System.out.println(msg);
                    } else if (startElement.getName().getLocalPart().equals("gw")) {
                        xmlEvent = xmlEventReader.nextEvent();
                        String msg = "";
                        if (!xmlEvent.asCharacters().getData().isEmpty()) {
                            msg = xmlEvent.asCharacters().getData();
                        }
                        System.out.println(msg);
                    } else if (startElement.getName().getLocalPart().equals("msg")) {
                        xmlEvent = xmlEventReader.nextEvent();
                        String msg = "";
                        if (!xmlEvent.asCharacters().getData().isEmpty()) {
                            msg = xmlEvent.asCharacters().getData();
                        }
                        System.out.println(msg);
                    }
                }
            }
        } catch (XMLStreamException | FileNotFoundException ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void parseXml(String fileName) {

    }

}
