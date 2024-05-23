package com.informationretrieval.informationfinal;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import search.Searcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


@SpringBootApplication
public class InformationFinalApplication {

    // Method to get context from a document and format it
    public static String getDocContext(Document d) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append((d.get("source_id"))).append("\n");
        sb.append(" ").append((d.get("year"))).append("\n");
        sb.append(" ").append((d.get("title"))).append("\n");
        sb.append(" ").append((d.get("abstract"))).append("\n");
        sb.append(" ").append((d.get("full_text"))).append("\n");
        return sb.toString();
    }
    public static void main(String[] args) {

        SpringApplication.run(InformationFinalApplication.class, args);
        System.setProperty("java.awt.headless", "false");

        // Set up main frame
        GridBagConstraints gbc = new GridBagConstraints();
        JFrame frame=new JFrame("Papers Search App");
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setSize(800,500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel=new JPanel();

        JTextField queryText= new JTextField();

        String[] listOfFields = {"","title","abstract","full_text"};
        JComboBox listOfOptions = new JComboBox(listOfFields);
        JComboBox listOfOptionsForOrder = new JComboBox(listOfFields);

        DefaultListModel listModel = new DefaultListModel();
        DefaultListModel historyModel = new DefaultListModel();
        JButton button=new JButton("Search");
        JButton orderButton=new JButton("Order by");
        JLabel queryDescription= new JLabel("Enter keyword");

        JLabel fieldDescription= new JLabel("Field");
        JButton nextSongBtn=new JButton("Next 10");

        // Create an anonymous inner class to store search-related data
        var searchInfo = new Object() {
            private int displayPapers = 0;
            Searcher searcher = null;
            int itemsToSearch=0;
            ArrayList<Document> results = new ArrayList<>();
            HashMap<String,Integer> history = new HashMap<>();
            ArrayList<String> historyList = new ArrayList<>();
        };
        // Initialize searcher
        try {
            searchInfo.searcher = new Searcher("src/main/java/index");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        JList list=new JList(listModel);

        // ActionListener for ordering results
        ActionListener orderHandler = o->{
            String orderedBy = String.valueOf(listOfOptionsForOrder.getSelectedItem());
            int orderIndex = 1;// Default ordering field

            // Map the selected field to the corresponding index
            switch (orderedBy) {
                case "title":
                    orderIndex = 3;
                    break;
                case "abstract":
                    orderIndex = 4;
                    break;
                case "full_text":
                    orderIndex = 5;
                    break;
                case "source_id":
                default:
                    orderIndex = 1;
                    break;
            }

            searchInfo.displayPapers =0;

            // Bubble sort the results based on the selected field
            for (int j = 0; j < searchInfo.itemsToSearch - 1; j++) {
                for (int k = 0; k < searchInfo.itemsToSearch - j - 1; k++) {
                    String value1 = String.valueOf(searchInfo.results.get(k).getFields().get(orderIndex));
                    String value2 = String.valueOf(searchInfo.results.get(k + 1).getFields().get(orderIndex));
                    if (value1 == null) value1 = "";
                    if (value2 == null) value2 = "";
                    if (value1.compareToIgnoreCase(value2) > 0) {
                        Document swap = searchInfo.results.get(k);
                        searchInfo.results.set(k, searchInfo.results.get(k + 1));
                        searchInfo.results.set(k + 1, swap);
                    }
                }
            }
            listModel.removeAllElements();

            // Display the sorted results
            if(searchInfo.results.size() > 10) {
                for (int j = searchInfo.displayPapers; j < searchInfo.displayPapers + 10; j++) {
                    listModel.addElement(getDocContext(searchInfo.results.get(j))
                            .replaceAll("stored,indexed,tokenized<[^:]*:", "").replaceAll(">", "").trim());
                }
            }
            else{
                for (int j = searchInfo.displayPapers; j < searchInfo.displayPapers + searchInfo.results.size(); j++) {
                    listModel.addElement(getDocContext(searchInfo.results.get(j))
                            .replaceAll("stored,indexed,tokenized<[^:]*:", "").replaceAll(">", "").trim());
                }
            }
        };

        // ActionListener for showing next 10 results
        ActionListener showHandler= h-> {
            searchInfo.displayPapers = 0;
            if(searchInfo.itemsToSearch > 10) {

                searchInfo.displayPapers += 10;
                listModel.removeAllElements();

                if((searchInfo.displayPapers +10) >= searchInfo.itemsToSearch) {
                    panel.remove(nextSongBtn);
                }

                int tempSearch = 10;

                if (searchInfo.itemsToSearch - searchInfo.displayPapers < 10){
                    tempSearch = searchInfo.itemsToSearch - searchInfo.displayPapers;
                }


                for (int j = searchInfo.displayPapers; j < (searchInfo.displayPapers + tempSearch); j++) {
                    listModel.addElement(getDocContext(searchInfo.results.get(j))
                            .replaceAll("stored,indexed,tokenized<[^:]*:", "").replaceAll(">", "").trim());

                }

            }

        };

        // ActionListener for search button
        ActionListener buttonHandler= e->{
            searchInfo.displayPapers = 0;
            String query = queryText.getText();
            String field = String.valueOf(listOfOptions.getSelectedItem());
            try {
                searchInfo.results = (ArrayList<Document>) searchInfo.searcher.search(query, field);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            searchInfo.itemsToSearch = searchInfo.results.size();

            // Save search query to history file
            try {
                FileWriter myWriter = new FileWriter("search-history.txt",true);
                BufferedWriter bw = new BufferedWriter(myWriter);
                bw.write(query);
                bw.newLine();
                bw.close();
                myWriter.close();
            } catch (IOException ex) {
                System.out.println("An error occurred.");
                ex.printStackTrace();
            }

            // Read and update search history
            File file = new File("search-history.txt");
            try {
                FileReader fr=new FileReader(file);
                BufferedReader br=new BufferedReader(fr);
                String line, lastSearchedKeyword = null;
                while((line=br.readLine())!=null)
                {
                    if(!searchInfo.history.containsKey(line)){
                        searchInfo.history.put(line,0);
                    }
                    lastSearchedKeyword = line;
                }

                if(searchInfo.history.containsKey(lastSearchedKeyword))
                {
                    searchInfo.history.replace(lastSearchedKeyword,searchInfo.history.get(lastSearchedKeyword)+1);
                }

                fr.close();
            }
            catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Update history list
            for(String s:searchInfo.history.keySet()){
                if(!searchInfo.historyList.contains(s)){
                    searchInfo.historyList.add(s);
                }

            }

            // Sort history by most searched
            for(int l=1; l<=searchInfo.historyList.size()-1;l++){
                for(int j=1;j<searchInfo.historyList.size()-1-l;j++){
                    if(searchInfo.history.get( searchInfo.historyList.get(j-1)).compareTo(searchInfo.history.get(searchInfo.historyList.get(j)))<0){
                        String temp=searchInfo.historyList.get(j-1);
                        searchInfo.historyList.set(j-1,searchInfo.historyList.get(j));
                        searchInfo.historyList.set(j,temp);
                    }
                }
            }
            historyModel.removeAllElements();
            nextSongBtn.addActionListener(showHandler);
            orderButton.addActionListener(orderHandler);
            panel.add(nextSongBtn);
            listModel.removeAllElements();

            // Display search results
            if(searchInfo.itemsToSearch >= 10) {
                for (int j = searchInfo.displayPapers; j < searchInfo.displayPapers + 10; j++) {
                    listModel.addElement(getDocContext(searchInfo.results.get(j))
                            .replaceAll("stored,indexed,tokenized<[^:]*:", "")
                            .replaceAll(">", "").trim());
                }
                historyModel.addElement("Most searched queries");
            }

            else {
                for (int j = searchInfo.displayPapers; j < searchInfo.itemsToSearch; j++) {
                    listModel.addElement(getDocContext(searchInfo.results.get(j))
                            .replaceAll("stored,indexed,tokenized<[^:]*:", "")
                            .replaceAll(">", "").trim());

                }
                historyModel.addElement("Most searched queries");
            }

            // Display top 10 search history items
            if(searchInfo.history.keySet().size()>=10) {
                for (int z = 0; z < 10; z++) {
                    historyModel.addElement((searchInfo.historyList).get(z));
                }
            }
            else{
                for (int z = 0; z <searchInfo.history.keySet().size(); z++) {
                    historyModel.addElement((searchInfo.historyList).get(z));
                }
            }
        };

        // Mouse listener for double-click on list items
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() % 2 == 0) {

                    int index = list.locationToIndex(evt.getPoint());

                    for (int k = 0; k < searchInfo.itemsToSearch; k++) {
                        if ((getDocContext(searchInfo.results.get(k))
                                .replaceAll("stored,indexed,tokenized<[^:]*:", "")
                                .replaceAll(">", " ")).equals(getDocContext(searchInfo.results.get(index))
                                .replaceAll("stored,indexed,tokenized<[^:]*:", "")
                                .replaceAll(">", " ")))
                        {
                            // Create a new frame to display detailed document info
                            JTextArea area = new JTextArea(
                                    searchInfo.results.get(k).getFields().get(1).toString()
                                            .replaceAll("stored,indexed,tokenized<source_id:", "")
                                            .replaceAll(">", " ") + "\n" +
                                            searchInfo.results.get(k).getFields().get(2).toString()
                                                    .replaceAll("stored,indexed,tokenized<year:", "")
                                                    .replaceAll(">", " ") + "\n" +
                                            searchInfo.results.get(k).getFields().get(3).toString()
                                                    .replaceAll("stored,indexed,tokenized<title:", "")
                                                    .replaceAll(">", " ")  + "\n" +
                                            searchInfo.results.get(k).getFields().get(4).toString()
                                                    .replaceAll("stored,indexed,tokenized<abstract_text:", "")
                                                    .replaceAll(">", " ") + "\n" +
                                            searchInfo.results.get(k).getFields().get(5).toString()
                                                    .replaceAll("stored,indexed,tokenized<full_text:", "")
                                                    .replaceAll(">", " ") + "\n" +
                                            searchInfo.results.get(k).getFields().get(6).toString()
                                                    .replaceAll("stored,indexed,tokenized<contents:", "")
                                                    .replaceAll(">", " ") + "\n"

                            );

                            String frameTitle =searchInfo.results.get(k).getFields().get(1).toString()
                                    .replaceAll("stored,indexed,tokenized<title:Title:", "")
                                    .replaceAll(">", " ");

                            JFrame detailFrame = new JFrame(frameTitle);
                            detailFrame.setVisible(true);
                            detailFrame.setResizable(true);
                            detailFrame.setSize(600,300);

                            detailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


                            area.setLineWrap(true);
                            area.setWrapStyleWord(true);

                            area.setVisible(true);
                            area.setBackground(new Color(22, 206, 3));
                            detailFrame.add(area);
                        }
                    }

                }
            };
        });

        JList historyList=new JList(historyModel);

        // Set background color for list and panel
        list.setBackground(new Color(3, 152, 198));
        button.addActionListener(buttonHandler);
        panel.setBackground(new Color(61, 76, 135));
        panel.setLayout(new GridBagLayout());
        gbc.insets=new Insets(1,1,1,1);

        // Add components to panel with GridBagConstraints
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(queryDescription,gbc);

        gbc.gridx=0;
        gbc.gridy=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(queryText,gbc);

        gbc.gridx=1;
        gbc.gridy=0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(fieldDescription,gbc);

        gbc.gridx=1;
        gbc.gridy=1;
        gbc.fill =GridBagConstraints.NONE;
        panel.add(listOfOptions,gbc);

        gbc.gridx=2;
        gbc.gridy=0;
        gbc.gridheight=2;
        gbc.fill=GridBagConstraints.VERTICAL;
        panel.add(button,gbc);


        gbc.gridx=3;
        gbc.gridy=0;
        gbc.gridheight=2;
        gbc.fill=GridBagConstraints.VERTICAL;
        panel.add(nextSongBtn,gbc);

        gbc.gridx=4;
        gbc.gridy=0;
        gbc.gridheight=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(orderButton,gbc);

        gbc.gridx=4;
        gbc.gridy=1;
        gbc.gridheight=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(listOfOptionsForOrder,gbc);


        gbc.gridx=0;
        gbc.gridy=3;
        gbc.gridwidth=10;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(list,gbc);

        gbc.gridx=0;
        gbc.gridy=6;
        gbc.gridheight=5;
        gbc.gridwidth=10;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(historyList,gbc);


        frame.add(panel);

    }
}
