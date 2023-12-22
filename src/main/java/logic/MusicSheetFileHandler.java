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
                throw new NotImplementedYetException("Missing feature: key signature");
            notes = doc.getElementsByTagName("note");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    //Parsing del file xml
    //Estrae solo il pitch di ogni nota (ignora le pause)
    //CORREGGERE LOGICA PER ALTERAZIONI NELLA STESSA BATTUTA
    public List<Integer> getSheetFromFile(){

        ArrayList<Integer> sheet = new ArrayList<>();

        for(int i = 0; i < notes.getLength(); i++) {

            Node node = notes.item(i);

            if(node.getNodeType() == Node.ELEMENT_NODE) {

                Element note = (Element) node;

                Element pitch = (Element) note.getElementsByTagName("pitch").item(0);

                if(pitch!=null && !isChord(note) && isRightHandStaff(note)){

                    //Non è una pausa, non è un accordo, non è il manuale sinistro

                    String sstep = pitch.getElementsByTagName("step").item(0).getTextContent();
                    int step = 0;
                    switch(sstep){
                        case "A": step=10; break;
                        case "B": step=12; break;
                        case "C": step=1; break;
                        case "D": step=3; break;
                        case "E": step=5; break;
                        case "F": step=6; break;
                        case "G": step=8; break;
                    }
                    Node acc = pitch.getElementsByTagName("accidental").item(0);
                    if(acc!=null){
                        String sacc = acc.getTextContent();
                        if(sacc.equals("sharp"))
                            step++;
                        else if(sacc.equals("flat"))
                            step--;
                    }
                    int octave = Integer.parseInt(pitch.getElementsByTagName("octave").item(0).getTextContent());
                    sheet.add(step+12*(octave-2));
                }

            }
        }
        return sheet;
    }

    public void writeFingeringOnFile(List<Integer> fingering) {

        int j = 0;

        for(int i = 0; i < notes.getLength(); i++) {

            Node node = notes.item(i);

            if(node.getNodeType() == Node.ELEMENT_NODE) {

                Element note = (Element) node;
                Element pitch = (Element) note.getElementsByTagName("pitch").item(0);

                if(pitch!=null && !isChord(note) && isRightHandStaff(note)){
                    Node notationsNode = doc.createElement("notations");
                    Node technicalNode = doc.createElement("technical");
                    Node fingeringNode = doc.createElement("fingering");
                    Node fingeringText = doc.createTextNode(fingering.get(j++).toString());
                    fingeringNode.appendChild(fingeringText);
                    technicalNode.appendChild(fingeringNode);
                    notationsNode.appendChild(technicalNode);
                    if(note.getElementsByTagName("notations").item(0)!=null)
                        node.replaceChild(notationsNode, note.getLastChild());
                    else
                        node.appendChild(notationsNode);
                }

            }
        }


        //Scrittura su file
        try {
            Transformer tf = TransformerFactory.newInstance().newTransformer();
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
}
