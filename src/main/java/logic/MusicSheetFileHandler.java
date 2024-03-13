package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class MusicSheetFileHandler {

    private File file;
    private NodeList notes;
    private Document doc;

    public MusicSheetFileHandler(File file) throws NotImplementedYetException {
        this.file = file;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(file);
            doc.getDocumentElement().normalize();
            if(hasKeySignature())
                throw new NotImplementedYetException("leggere le alterazioni in chiave");
            notes = doc.getElementsByTagName("note");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    //Parsing del file xml
    //Estrae solo il tono e l'ottava di ogni nota, li salva in un unico valore
    public List<List<Integer>> getSheetFromFile() throws NotImplementedYetException {

        List<List<Integer>> sheet = new ArrayList<>();
        List<Integer> phrase = new ArrayList<>();

        boolean lastClefWasG = true;
        String lastMeasureNumber = "";
        List<Integer> measureAccidentals = new ArrayList<>();

        for(int i = 0; i < notes.getLength(); i++) {

            Element note = (Element) notes.item(i);
            Element pitch = (Element) note.getElementsByTagName("pitch").item(0);
            Element measure = (Element) note.getParentNode();
            String measureNumber = measure.getAttribute("number");
            if(!measureNumber.equals(lastMeasureNumber) && !measureAccidentals.isEmpty()) {
                measureAccidentals.clear();
                lastMeasureNumber = measureNumber;
            }

            if((!measureHasAClef(note)&&lastClefWasG)||(measureHasAClef(note)&&isGClef(note))){

                //Siamo in chiave di violino
                lastClefWasG = true;

                if(pitch!=null && !isChord(note) && isRightHandStaff(note)){

                    //Non è una pausa, non è un accordo, non è il manuale sinistro
                    //La nota viene presa in considerazione dal problema

                    String sstep = pitch.getElementsByTagName("step").item(0).getTextContent();
                    int step = 0;
                    switch (sstep) {
                        case "A":
                            step = 10;
                            break;
                        case "B":
                            step = 12;
                            break;
                        case "C":
                            step = 1;
                            break;
                        case "D":
                            step = 3;
                            break;
                        case "E":
                            step = 5;
                            break;
                        case "F":
                            step = 6;
                            break;
                        case "G":
                            step = 8;
                            break;
                    }

                    //Il tono alterato è inserito nella lista delle alterazioni con valore positivo se diesis e negativo se bemolle

                    //Ricerca di eventuali alterazioni sulla nota
                    Node acc = note.getElementsByTagName("accidental").item(0);
                    if (acc != null) {
                        String sacc = acc.getTextContent();
                        switch (sacc) {
                            case "sharp":
                                measureAccidentals.add(step);
                                step++;
                                break;
                            case "flat":
                                measureAccidentals.add(-step);
                                step--;
                                break;
                            case "natural":
                                measureAccidentals.remove((Object) step);
                                measureAccidentals.remove((Object) (step * -1));
                                break;
                            default:
                                throw new NotImplementedYetException("leggere segni di alterazione diversi da diesis, bemolle e bequadro");
                        }
                    }

                    //Ricerca di eventuali alterazioni nella battuta
                    else if(measureAccidentals.contains(step))
                        step++;
                    else if(measureAccidentals.contains(-step))
                        step--;

                    int octave = Integer.parseInt(pitch.getElementsByTagName("octave").item(0).getTextContent());
                    phrase.add(step + 12 * (octave - 2));
                } else if(isRest(note) && !phrase.isEmpty()){
                    //Dopo ogni pausa, comincia una nuova frase
                    sheet.add(phrase);
                    phrase = new ArrayList<>();
                }
            } else{
                lastClefWasG = false;
            }
        }

        if(!phrase.isEmpty())
            sheet.add(phrase);

        return sheet;
    }

    public void writeFingeringOnFile(List<Integer> fingering) {

        int j = 0;
        boolean lastClefWasG = true;

        for(int i = 0; i < notes.getLength(); i++) {

            Node node = notes.item(i);

            if(node.getNodeType() == Node.ELEMENT_NODE) {

                Element note = (Element) node;
                Element pitch = (Element) note.getElementsByTagName("pitch").item(0);

                if((!measureHasAClef(note)&&lastClefWasG)||(measureHasAClef(note)&&isGClef(note))) {

                    lastClefWasG = true;

                    if (pitch != null && !isChord(note) && isRightHandStaff(note)) {
                        Node notationsNode = doc.createElement("notations");
                        Node technicalNode = doc.createElement("technical");
                        Node fingeringNode = doc.createElement("fingering");
                        Node fingeringText = doc.createTextNode(formatRealFinger(fingering.get(j++)));
                        fingeringNode.appendChild(fingeringText);
                        technicalNode.appendChild(fingeringNode);
                        notationsNode.appendChild(technicalNode);
                        if (note.getElementsByTagName("notations").item(0) != null)
                            node.replaceChild(notationsNode, note.getLastChild());
                        else
                            node.appendChild(notationsNode);
                    }
                } else{
                    lastClefWasG = false;
                }

            }
        }

        //Scrittura su file
        try {
            //Transformer tf = TransformerFactory.newInstance().newTransformer(new StreamSource(new File("main/resources/XSLT.xslt")));
            Transformer tf = TransformerFactory.newInstance().newTransformer(new StreamSource(getClass().getResourceAsStream("/XSLT.xslt")));
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.METHOD, "xml");
            tf.setOutputProperty(OutputKeys.STANDALONE, "no");

            DOMSource domSource = new DOMSource(doc);
            StreamResult sr = new StreamResult(file);
            tf.transform(domSource, sr);

        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    //Semplificazione #1: solo manuale destro
    private boolean isRightHandStaff(Element note){
        Element staff = (Element) note.getElementsByTagName("staff").item(0);
        return staff == null || staff.getTextContent().equals("1");
    }

    //Semplificazione #2: niente polifonia
    private boolean isChord(Element note){
        return note.getElementsByTagName("chord").item(0)!=null;
    }

    //Semplificazione #3: niente alterazioni in chiave
    private boolean hasKeySignature(){
        NodeList fifths = doc.getElementsByTagName("fifths");
        for(int i=0; i< fifths.getLength(); i++)
            if (!fifths.item(i).getTextContent().equals("0"))
                return true;
        return false;
    }

    //Semplificazione #4: solo chiave di violino
    private boolean isGClef(Element note){
        Element measure = (Element) note.getParentNode();
        Element attributes = (Element) measure.getElementsByTagName("attributes").item(0);
        Element clef = (Element) attributes.getElementsByTagName("clef").item(0);
        Element sign = (Element) clef.getElementsByTagName("sign").item(0);
        return sign.getTextContent().equals("G");
    }

    private boolean measureHasAClef(Element note){
        Element measure = (Element) note.getParentNode();
        Element attributes = (Element) measure.getElementsByTagName("attributes").item(0);
        if(attributes==null)
            return false;
        Element clef = (Element) attributes.getElementsByTagName("clef").item(0);
        if(clef==null)
            return false;
        Element sign = (Element) clef.getElementsByTagName("sign").item(0);
        return sign != null;
    }

    private boolean isRest(Element note){
        return note.getElementsByTagName("rest").item(0) != null;
    }

    private String formatRealFinger(Integer finger){
        if (finger > 5) {
            finger = finger % 6 + 1;
            return "(" + finger + ")";
        }
        return finger.toString();
    }
}
