package Interfaz;
/**
 * @author  H�ctor Al�n De La Fuente Anaya.
 * @version 1
 * @since Diciembre 2018
 */
import Modelo.BPMNModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.SwingUtilities;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import javax.swing.JSeparator;

// Clases de la libreria

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;

public class VisualizadorDeProcesos {

   public static void main(String args[]) {
      ProcessViewer processViewer = new ProcessViewer();
   }
}

class ProcessViewer {

   private JFrame main_frm;
   private File fileName;
   private JPanel loadFile_pnl, menu_pnl, viewer_pnl, raw_pnl;
   private JButton raw_btn, information_btn, traces_btn, activities_btn, loadFile_btn, exportAsCSV_btn, deployment_btn, model_btn, exportAsXES_btn;
   private JLabel titulo_txt;
   private DefaultTableModel raw_dtm, traces_dtm, activities_dtm, information_dtm, model_dtm;
   private ActionListener loadFile_btnAction, raw_btnAction, information_btnAction, traces_btnAction, activities_btnAction, exportAsCSV_btnAction, exportAsXES_btnAction, model_btnAction, deployment_btnAction;
   private boolean rawSelected, activitiesSelected, tracesSelected, informationSelected, modelSelected, deploymentSelected;
   private String deployment;
   static BPMNModel BPMN = new BPMNModel();

   public ProcessViewer() {
   // Valores de inicio de porgrama
      rawSelected = true;
      activitiesSelected = false;
      tracesSelected = false;
      informationSelected = false;
      modelSelected = false;
      deploymentSelected = false;
      deployment = "";
      fileName = null;
      
   // Se generan las acciones
      initializeActions();
   
   // Se arma la interfaz
      buildLoadFile();
      buildMenu();
      buildRaw();
      buildViewer();
      buildWindow();
   
   }

   private void buildLoadFile() {
   // Se iicializa el panel
      loadFile_pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      loadFile_pnl.setBackground(Color.black);
      
   // Se generan los elementos
      titulo_txt = new JLabel();
      titulo_txt.setForeground(Color.white);
      titulo_txt.setFont(new Font("Tahoma", Font.BOLD, 15));
      if (fileName != null) {
         titulo_txt.setText(fileName.getName());
         loadFile_pnl.add(titulo_txt);
         if (fileName.getAbsolutePath().contains(".xes")) {
            exportAsCSV_btn = new JButton("Export as CSV");
            exportAsCSV_btn.addActionListener(exportAsCSV_btnAction);
            loadFile_pnl.add(exportAsCSV_btn);
         } else if (fileName.getAbsolutePath().contains(".csv") || fileName.getAbsolutePath().contains(".txt")) {
            exportAsXES_btn = new JButton("Export as XES");
            exportAsXES_btn.addActionListener(exportAsXES_btnAction);
            loadFile_pnl.add(exportAsXES_btn);
         }
      } else {
         titulo_txt.setText("No selected file");
         loadFile_pnl.add(titulo_txt);
      }
   
      loadFile_btn = new JButton("Load File");
      loadFile_btn.addActionListener(loadFile_btnAction);
      loadFile_pnl.add(loadFile_btn);
   
   }

   private void buildMenu() {
      // Se inicializa el panel
      menu_pnl = new JPanel();
   
      // Se inicializan los botones del menu
      raw_btn = new JButton("Dataset");
      information_btn = new JButton("Metadata");
      traces_btn = new JButton("Traces");
      activities_btn = new JButton("Ativities");
      deployment_btn = new JButton("Deploy");
      model_btn = new JButton("Model");
   
      // Se asigan acciones a los botones
      raw_btn.addActionListener(raw_btnAction);
      information_btn.addActionListener(information_btnAction);
      activities_btn.addActionListener(activities_btnAction);
      traces_btn.addActionListener(traces_btnAction);
      model_btn.addActionListener(model_btnAction);
      deployment_btn.addActionListener(deployment_btnAction);
   
   
      // Se le asignan el corresponediente color para identificar que los han seleccionado
      if (rawSelected) {
         raw_btn.setBackground(Color.LIGHT_GRAY);
      } else {
         raw_btn.setBackground(new JButton().getBackground());
      }
      if (informationSelected) {
         information_btn.setBackground(Color.LIGHT_GRAY);
      } else {
         information_btn.setBackground(new JButton().getBackground());
      }
      if (activitiesSelected) {
         activities_btn.setBackground(Color.LIGHT_GRAY);
      } else {
         activities_btn.setBackground(new JButton().getBackground());
      }
      if (tracesSelected) {
         traces_btn.setBackground(Color.LIGHT_GRAY);
      } else {
         traces_btn.setBackground(new JButton().getBackground());
      }
      if (modelSelected) {
         model_btn.setBackground(Color.LIGHT_GRAY);
      } else {
         model_btn.setBackground(new JButton().getBackground());
      }
      if (deploymentSelected) {
         deployment_btn.setBackground(Color.LIGHT_GRAY);
      } else {
         deployment_btn.setBackground(new JButton().getBackground());
      }
   
      // Se asigna un tama�o
      raw_btn.setPreferredSize(new Dimension(100, 30));
      information_btn.setPreferredSize(new Dimension(100, 30));
      activities_btn.setPreferredSize(new Dimension(100, 30));
      traces_btn.setPreferredSize(new Dimension(100, 30));
      deployment_btn.setPreferredSize(new Dimension(100, 30));
      model_btn.setPreferredSize(new Dimension(100, 30));
   
      // Se genera el contenedor
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.weightx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      JPanel buttons_pnl = new JPanel(new GridBagLayout());
   
      // Se agregan los botones al contenedor
      buttons_pnl.add(raw_btn, gbc);
      buttons_pnl.add(new JPanel(), gbc);
      buttons_pnl.add(information_btn, gbc);
      buttons_pnl.add(new JPanel(), gbc);
      buttons_pnl.add(activities_btn, gbc);
      buttons_pnl.add(new JPanel(), gbc);
      buttons_pnl.add(traces_btn, gbc);
      buttons_pnl.add(new JPanel(), gbc);
   
      //Se agrega un Separador
      buttons_pnl.add(new JSeparator(), gbc);
      buttons_pnl.add(new JPanel(), gbc);
   
      // Se siguen agregando botones
      buttons_pnl.add(model_btn, gbc);
      buttons_pnl.add(new JPanel(), gbc);
      buttons_pnl.add(deployment_btn, gbc);
      buttons_pnl.add(new JPanel(), gbc);
   
      // Se agrega el contenedor al panel
      menu_pnl.add(buttons_pnl);
   }

   /*
    * Funcion para generar un panel para visualizar tablas con barra de scroll
    **/
   JPanel buildTablePanel(DefaultTableModel dtb, String title) {
      /* Se inicializa la tablas*/
      JLabel titleTable_txt = new JLabel(title);
      titleTable_txt.setFont(new Font("Tahoma", Font.BOLD, 18));
      JScrollPane tbl_sp = new JScrollPane(new JTable(dtb));
   
      /* Se prepara el panel*/
      JPanel pnl = new JPanel(new BorderLayout());
      pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      pnl.add(titleTable_txt, BorderLayout.NORTH);
      pnl.add(tbl_sp, BorderLayout.CENTER);
   
      return pnl;
   }

   private void buildRaw() {
   
      raw_pnl = new JPanel();
   
      JPanel north = new JPanel();
      if (rawSelected) {
         north.add(buildTablePanel(raw_dtm, "Dataset"));
      }
      if (informationSelected) {
         north.add(buildTablePanel(information_dtm, "Metadata"));
      }
      north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));
      raw_pnl.add(north);
   
      JPanel south = new JPanel();
      if (activitiesSelected) {
         south.add(buildTablePanel(activities_dtm, "Activities"));
      }
      if (tracesSelected) {
         south.add(buildTablePanel(traces_dtm, "Detected Traces"));
      }
      if (modelSelected) {
         south.add(buildTablePanel(model_dtm, "BPMN Model"));
      }
      south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
      raw_pnl.add(south);
      
      if (deploymentSelected) {
         JPanel feet = new JPanel();
         
         JTextPane editor = new JTextPane();
         editor.setEditable(false);
         editor.setSize(400, 20);
         SimpleAttributeSet attrs = new SimpleAttributeSet();
         StyleConstants.setBold(attrs, true);
         StyleConstants.setFontFamily(attrs, "Courier New");
         try{
         editor.getStyledDocument().insertString(editor.getStyledDocument().getLength(), deployment, attrs); 
         }catch(Exception e){
         
         }
         JScrollPane dep_sp = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         dep_sp.setMinimumSize(new Dimension(10,40));
         /* Se prepara el panel Raw*/
         JPanel pnl = new JPanel(new BorderLayout());
         pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         JLabel title = new JLabel("Deployment");
         title.setFont(new Font("Tahoma", Font.BOLD, 18));

         pnl.add(title, BorderLayout.NORTH);
         pnl.add(dep_sp, BorderLayout.CENTER);
         
         feet.add(pnl);
         feet.setLayout(new BoxLayout(feet, BoxLayout.X_AXIS));
         
         raw_pnl.add(feet);
      }
   
      raw_pnl.setLayout(new BoxLayout(raw_pnl, BoxLayout.Y_AXIS));
   }

   private void buildViewer() {
      viewer_pnl = new JPanel(new CardLayout());
      viewer_pnl.add(raw_pnl, "raw");
   }

   private void buildWindow() {
      main_frm = new JFrame("Process Viewer");
      main_frm.setLayout(new BorderLayout());
      main_frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
   
      Toolkit t = Toolkit.getDefaultToolkit();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Se consigue el tama�o de la ventana
   
      main_frm.setSize(screenSize.width / 3 * 2, screenSize.height / 3 * 2);
      main_frm.setMaximumSize(new Dimension(screenSize.width, screenSize.height));
      main_frm.setMinimumSize(new Dimension(screenSize.width / 3 * 2, screenSize.height / 3 * 2));
      main_frm.setLocationRelativeTo(null);
      main_frm.setAlwaysOnTop(false);
   
      main_frm.add(loadFile_pnl, BorderLayout.NORTH);
      main_frm.add(menu_pnl, BorderLayout.WEST);
      main_frm.add(viewer_pnl, BorderLayout.CENTER);
      main_frm.pack();
   
      main_frm.setVisible(true);
   }

   private void initializeActions() {
   
      /* Accion del boton LoadFile */
      loadFile_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            
            /* Se escoge el archivo */
               JFileChooser fileChooser = new JFileChooser();
               fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
               fileChooser.setDialogTitle("Select a dataset file");
               fileChooser.setAcceptAllFileFilterUsed(false);
               fileChooser.setFileHidingEnabled(true);
               fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TXT, CSV & XES files", "txt", "csv", "xes"));
               fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TXT files", "txt"));
               fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
               fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("XES files", "xes"));
            
               if (fileName != null) {
                  fileChooser.setCurrentDirectory(fileName);
               }
            
               int result = fileChooser.showOpenDialog(fileChooser);
            
               if (result != JFileChooser.CANCEL_OPTION) {
               
                  fileName = fileChooser.getSelectedFile();
               
                  if ((fileName == null) || (fileName.getName().equals(""))) {
                     System.out.println("Error en el archivo.");
                  
                  } else {
                     System.out.println(fileName.getAbsolutePath());
                     System.out.println(fileName.getName());
                     if (!fileName.getName().endsWith(".txt") && !fileName.getName().endsWith(".csv") && !fileName.getName().endsWith(".xes")) {
                        System.out.println("El tipo de archivo de entrada no es valido.");
                     }
                  }
               }
               if (fileName != null) {
               
                  if (fileName.getName().endsWith(".txt") || fileName.getName().endsWith(".csv")) {
                  /* Se Lee archivo */
                     BufferedReader br = null;
                     String[] columnNames = null;
                     try {
                        br = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                        String line = br.readLine();
                     // Se cargan los nombre de las columnas
                        if (line != null) {
                           columnNames = line.split(";|,");
                        }
                     
                     // Se cargan los datos
                        ArrayList<String[]> filas = new ArrayList<String[]>();
                        line = br.readLine();
                        while (null != line) {
                           String[] fila = line.split(";|,");
                           filas.add(fila);
                           line = br.readLine();
                        }
                     
                     // se juntan las filas en una matriz
                        String[][] data = new String[filas.size()][columnNames.length];
                        for (int i = 0; i < filas.size(); i++) {
                           for (int j = 0; j < columnNames.length; j++) {
                              data[i][j] = filas.get(i)[j];
                           }
                        }
                     
                     // Modelo para la table Raw
                        raw_dtm = new DefaultTableModel(data, columnNames);
                     
                     ///////Se jacen los modelos de la informacion procesada
                     // Se consigue la informacion
                        readDataInput(fileName.getAbsolutePath());
                     
                     // Se reconstruye la ventana
                        refreshWindow();
                     
                     } catch (Exception e) {
                        System.out.println(e);
                     } finally {
                        if (null != br) {
                           try {
                              br.close();
                           } catch (Exception e) {
                           
                           }
                        }
                     }
                  } else if (fileName.getName().endsWith(".xes")) {
                     XFactoryBufferedImpl factory = new XFactoryBufferedImpl();
                     XesXmlParser xesXmlParser = new XesXmlParser();
                  
                     try {
                        List<XLog> list = xesXmlParser.parse(new File(fileName.getAbsolutePath()));
                     
                        XAttributeMap xam = list.get(0).get(0).get(0).getAttributes();
                        ArrayList<String> gea = new ArrayList<String>();
                        xam.forEach((k, v) -> gea.add(k));
                     
                        System.out.println(gea.size());
                        for (int i = 0; i < gea.size(); i++) {
                           System.out.println(gea.get(i));
                        }
                     
                     //List<XAttribute> gea = list.get(0).getGlobalEventAttributes();
                        String[] columnNames = new String[gea.size() + 1];
                        ArrayList<String[]> filas = new ArrayList<String[]>();
                     
                        columnNames[0] = "Case ID";
                        for (int j = 1; j < gea.size() + 1; j++) {
                           columnNames[j] = gea.get(j - 1);
                        }
                     
                        for (int i = 0; i < list.size(); i++) {
                           List<XTrace> le = list.get(i);
                        
                           for (int j = 0; j < le.size(); j++) {
                              XTrace trace = le.get(j);
                           
                              XAttribute a = trace.getAttributes().get("concept:name");
                              if (a == null) {
                                 a = trace.getAttributes().get("Case ID");
                              }
                           
                              for (int k = 0; k < trace.size(); k++) {
                                 XEvent event = trace.get(k);
                              
                                 String[] fila = new String[gea.size() + 1];
                                 fila[0] = a + "";
                              
                                 for (int l = 1; l <= gea.size(); l++) {
                                    fila[l] = event.getAttributes().get(gea.get(l - 1)) + "";
                                 }
                                 filas.add(fila);
                              }
                           }
                        }
                     
                     // Se juntan las filas en una matriz
                        String[][] data = new String[filas.size()][columnNames.length];
                        for (int i = 0; i < filas.size(); i++) {
                           for (int j = 0; j < columnNames.length; j++) {
                              data[i][j] = filas.get(i)[j];
                           }
                        }
                     
                     // Modelo para la table Raw
                        raw_dtm = new DefaultTableModel(data, columnNames);
                     
                     ///////Se jacen los modelos de la informacion procesada
                     // Se consigue la informacion
                        readDataInput(fileName.getAbsolutePath());
                     
                     // Se reconstruye la ventana
                        refreshWindow();
                     
                     } catch (Exception ex) {
                        System.out.println(ex);
                     }
                  
                  }
               }
            }
         
         };
   
      exportAsXES_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            
            /* Se escoge el directorio destino para el archivo */
               JFileChooser fileChooser = new JFileChooser();
               fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
               fileChooser.setDialogTitle("Select the destination path");
               int result = fileChooser.showOpenDialog(fileChooser);
            
               File filePath = null;
               if (result != JFileChooser.CANCEL_OPTION) {
                  filePath = fileChooser.getSelectedFile();
                  if ((filePath == null) || (filePath.getAbsolutePath().equals(""))) {
                     System.out.println("Error la ruta.");
                  } else {
                     System.out.println(filePath.getAbsolutePath());
                  }
               }
            
               if (filePath != null) {
               
                  XFactoryBufferedImpl factory = new XFactoryBufferedImpl();
               
                  try {
                  
                     BufferedReader br = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                  
                     String[] columnNames = br.readLine().split(";|,");
                  
                  // Se consigue el numero de columna de Case ID
                     int caseIdColumn = -1;
                  
                     if (columnNames != null) {
                        for (int i = 0; i < columnNames.length; i++) {
                           if (columnNames[i].equals("Case ID")) {
                              caseIdColumn = i;
                           }
                           break;
                        }
                     }
                  
                     System.out.println("caseIdColumn : " + caseIdColumn);
                     if (caseIdColumn != -1) {
                     
                     // Se cargan el resto de las filas
                        ArrayList<String[]> data = new ArrayList<String[]>();
                        String linea = br.readLine();
                        while (linea != null) {
                           String[] fila = linea.split(";|,");
                           data.add(fila);
                           linea = br.readLine();
                        }
                     
                     //Se consiguen todos los id
                        ArrayList<String> ids = new ArrayList<String>();
                     
                        for (int i = 0; i < data.size(); i++) {
                           if (!ids.contains(data.get(i)[caseIdColumn])) {
                              ids.add(data.get(i)[caseIdColumn]);
                           }
                        }
                     
                     //Se crea el archivo xes
                        XLog log = factory.createLog();
                        for (int i = 0; i < ids.size(); i++) {
                        // Atributo del trace
                           XAttributeMap atmT = factory.createAttributeMap();
                           atmT.put("Case ID", factory.createAttributeLiteral("Case ID", ids.get(i), null));
                           XTrace trace = factory.createTrace(atmT);
                        
                        //Eventos del trace
                           for (int j = 0; j < data.size(); j++) {
                              if (data.get(j)[caseIdColumn].equals(ids.get(i))) {
                              
                                 XAttributeMap atmE = factory.createAttributeMap();
                              
                                 for (int k = 0; k < columnNames.length; k++) {
                                    if (k != caseIdColumn) {
                                    
                                       atmE.put(columnNames[k], factory.createAttributeLiteral(columnNames[k], data.get(j)[k], null));
                                    
                                    }
                                 }
                                 trace.add(factory.createEvent(atmE));
                              }
                           }
                           log.add(trace);
                        }
                     
                        XesXmlSerializer xxs = new XesXmlSerializer();
                        if (fileName.getName().contains(".csv")) {
                           xxs.serialize(log, new FileOutputStream(filePath.getAbsolutePath() + "\\" + fileName.getName().replace(".csv", ".xes")));
                        } else if (fileName.getName().contains(".txt")) {
                           xxs.serialize(log, new FileOutputStream(filePath.getAbsolutePath() + "\\" + fileName.getName().replace(".txt", ".xes")));
                        }
                     
                     } else {
                        System.out.println("No se encontr� 'Case ID' en las columnas del archivo.");
                     }
                  
                  } catch (Exception ex) {
                     System.out.println(ex);
                  }
               }
            
            // Se reconstruye la ventana
               refreshWindow();
            }
         };
   
      exportAsCSV_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            
            /* Se escoge el directorio destino para el archivo */
               JFileChooser fileChooser = new JFileChooser();
               fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
               fileChooser.setDialogTitle("Select the destination path");
               int result = fileChooser.showOpenDialog(fileChooser);
            
               File filePath = null;
            
               if (result != JFileChooser.CANCEL_OPTION) {
                  filePath = fileChooser.getSelectedFile();
                  if ((filePath == null) || (filePath.getAbsolutePath().equals(""))) {
                     System.out.println("Error la ruta.");
                  } else {
                     System.out.println(filePath.getAbsolutePath());
                  }
               }
            
               if (filePath != null) {
               
                  FileWriter fileWriter = null;
                  try {
                     fileWriter = new FileWriter(filePath.getAbsolutePath() + "\\" + fileName.getName().replace(".xes", ".csv"));
                  
                  //Escribe el nombre de las columnas en el CSV
                     for (int i = 0; i < raw_dtm.getColumnCount(); i++) {
                        fileWriter.append(raw_dtm.getColumnName(i));
                        if (i != raw_dtm.getColumnCount() - 1) {
                           fileWriter.append(";");
                        }
                     }
                  
                     fileWriter.append("\n");
                  
                  //Escribe los datos en le csv
                     for (int i = 0; i < raw_dtm.getRowCount(); i++) {
                        for (int j = 0; j < raw_dtm.getColumnCount(); j++) {
                           fileWriter.append(raw_dtm.getValueAt(i, j).toString());
                           if (j != raw_dtm.getColumnCount() - 1) {
                              fileWriter.append(";");
                           }
                        }
                        fileWriter.append("\n");
                     }
                  
                     System.out.println("El archivo CSV fue creado con exito.");
                  
                  } catch (Exception e) {
                     System.out.println("Error en el programa.");
                     e.printStackTrace();
                  } finally {
                     try {
                        fileWriter.flush();
                        fileWriter.close();
                     } catch (IOException ex) {
                        System.out.println("Error al cerrar el frlujo del archivo.");
                     }
                  }
               }
            
            // Se reconstruye la ventana
               refreshWindow();
            }
         };
   
      traces_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               if (tracesSelected) {
                  tracesSelected = false;
               } else {
                  tracesSelected = true;
               }
            
            // Se reconstruye la ventana
               refreshWindow();
            }
         };
   
      raw_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               if (rawSelected) {
                  rawSelected = false;
               } else {
                  rawSelected = true;
               }
            
            // Se reconstruye la ventana
               refreshWindow();
            }
         };
   
      information_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               if (informationSelected) {
                  informationSelected = false;
               } else {
                  informationSelected = true;
               }
            
            // Se reconstruye la ventana
               refreshWindow();
            }
         };
   
      activities_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               if (activitiesSelected) {
                  activitiesSelected = false;
               } else {
                  activitiesSelected = true;
               }
            
            // Se reconstruye la ventana
               refreshWindow();
            }
         };
         
      model_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               if (modelSelected) {
                  modelSelected = false;
               } else {
                  modelSelected = true;
               }
            
            // Se reconstruye la ventana
               refreshWindow();
            }
         };
         
      deployment_btnAction = 
         new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               if (deploymentSelected) {
                  deploymentSelected = false;
               } else {
                  deploymentSelected = true;
               }
            
            // Se reconstruye la ventana
               refreshWindow();
            }
         };
   
   }

   // Funcion que procesa la informaci�n del Dataset 
   public void readDataInput(String filename) throws Exception {
   
      ArrayList<ArrayList<String>> bitacora = new ArrayList<ArrayList<String>>();
   
      int index;
      int i = 0;
      String inputLine = null;
      BufferedReader rd = new BufferedReader(new FileReader(new File(filename)));
   
      if (filename.contains(".csv") || filename.contains(".txt")) {
         inputLine = rd.readLine();                    //first line is the header, next lines are the data
         if (inputLine != null) {                      //determinar #columnas y crear ese nuemero de listas (vectores para los valores por columna)
            // String [] temp = inputLine.split(";");
            String[] temp = inputLine.split(";|,");// solo utilizar en caso de tener files con espacio
            for (i = 0; i < temp.length; i++) {
               bitacora.add(new ArrayList<String>());
            }
         }
         //para todas las lineas subsecuentes, leer los datos por columnas
         do {
            String[] temp = inputLine.split(";|,");
            for (i = 0; i < temp.length; i++) {
               bitacora.get(i).add(temp[i].trim());
            }
         } while ((inputLine = rd.readLine()) != null);
      
         /* En caso de ser un archivo en formato xes, se utiliza la librer�a 
         OpenXES para extraer los datos linea por linea. */
      } else if (filename.contains(".xes")) {
         // Se crean las columnas
         bitacora.add(new ArrayList<String>());
         bitacora.add(new ArrayList<String>());
         bitacora.get(0).add("Case ID");
         bitacora.get(1).add("Activity");
      
         XesXmlParser xesXmlParser = new XesXmlParser();
      
         try {
            List<XLog> list = xesXmlParser.parse(new File(filename));
         
            for (int e = 0; e < list.size(); e++) {
               List<XTrace> le = list.get(e);
            
               for (int j = 0; j < le.size(); j++) {
                  XTrace trace = le.get(j);
                  XAttribute a = trace.getAttributes().get("concept:name");
                  if (a == null) {
                     a = trace.getAttributes().get("Case ID");
                  }
               
                  for (int k = 0; k < trace.size(); k++) {
                     XEvent event = trace.get(k);
                     bitacora.get(0).add(a + "");
                     bitacora.get(1).add(event.getAttributes().get("Activity") + "");
                  
                  }
               }
            
            }
            i = 2;
         } catch (Exception ex) {
            System.out.println(ex);
         }
      
      } else {
         i = -1;
      }
   
      rd.close();
   
      //procede a obtener la lista de actividades
      int numCols = i;
   
      LinkedHashMap<String, Character> activityList = new LinkedHashMap<String, Character>();
      char value;
      //ascii code in literal a    
      int s = 97;
      String strVal = null;
      ArrayList<String> listValues = null;
   
      int k = 0;
      while (!bitacora.get(k).get(0).equals("Activity") && (k < numCols)) {
         k++;
      }
   
      listValues = bitacora.get(k);   //recupera la lista 'Activity' y su tamaño
      int sizeList = listValues.size();
   
      for (int j = 1; j < sizeList; j++) {
         value = (char) (s);
         String activityName = listValues.get(j);
         if (!(activityList.containsKey(activityName))) {
            activityList.put(activityName, value);
            s = s + 1;
         }
      }
   
      //recupera el set de tasks T
      Set<Map.Entry<String, Character>> tasks = activityList.entrySet();
      int tam = activityList.size();
      Character task;
   
      for (Map.Entry<String, Character> entry : tasks) {
         task = entry.getValue();
         BPMN.T.add(task);
      }
   
      //procede a calcular la lista de trazas
      LinkedHashMap<Integer, ArrayList<Character>> tracesList = new LinkedHashMap<Integer, ArrayList<Character>>();
      ArrayList<Character> traces = new ArrayList<Character>();
   
      String key;
      int ID = 0, IDnext = 0;
      String colName = "";
      ArrayList<String> listCASE_ID = null;
      ArrayList<String> listACTIVITY = null;
   
      int tamList = bitacora.get(0).size();
   
      //recupera la lista de CASE_ID y ACTIVITY
      for (k = 0; k < numCols; k++) {
         if (bitacora.get(k).get(0).equals("Case ID")) {
            listCASE_ID = bitacora.get(k);
         } else if (bitacora.get(k).get(0).equals("Activity")) {
            listACTIVITY = bitacora.get(k);
         } else {
            continue;
         }
      }
   
      ID = Integer.parseInt(listCASE_ID.get(1));
      //recorre las listas
      for (int j = 1; j < tamList; j++) {
      
         IDnext = Integer.parseInt(listCASE_ID.get(j));
      
         if (IDnext != ID) {
            tracesList.put(ID, traces);
            ID = IDnext;
            traces = new ArrayList<Character>();
         }
      
         key = listACTIVITY.get(j);
         value = activityList.get(key);
         traces.add(value);
      }
   
      tracesList.put(ID, traces);  //la ultima
   
      // Se dempieza a separar los datos
      String[] columnNames = null;
      String[][] data = null;
   
      // Se prepara la tabla Actividades
      columnNames = new String[]{"Description", "Item"};
      data = new String[activityList.size()][2];
      i = 0;
      for (Map.Entry<String, Character> entry1 : activityList.entrySet()) {
         data[i][0] = entry1.getKey();
         data[i][1] = String.valueOf(entry1.getValue());
         i++;
      }
   
      // Modelo para la table activities
      activities_dtm = new DefaultTableModel(data, columnNames);
   
      ArrayList<ArrayList<Character>> uno = new ArrayList<ArrayList<Character>>(tracesList.values());
   
      //Se consigue la informacion con tracesList
      int min = uno.get(0).size();
      int max = -1;
      int suma = 0;
      int total = 0;
   
      for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
         int t = entry.getValue().size();
         total = total + t;
         if (t < min) {
            min = t;
         } else {
            if (max < t) {
               max = t;
            }
         }
      
         suma = suma + t;
      }
      float average = (float) suma / tracesList.size();
   
      // Se prepara el modelo para la table infrmation
      columnNames = new String[]{"", ""};
      data = new String[][]{{"Activities", "" + activityList.size()},
         {"Traces", "" + tracesList.size()},
         {"Events", "" + total},
         {"Minimum events per trace", "" + min},
         {"Maximum events per trace", "" + max},
         {"Trace size average", "" + average}};
   
      // Modelo para la table information
      information_dtm = new DefaultTableModel(data, columnNames);
   
      // Se prepara la tabla traces
      columnNames = new String[]{"ID", "Traces"};
      data = new String[tracesList.size()][2];
      i = 0;
      for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
         data[i][0] = String.valueOf(entry.getKey());
         data[i][1] = String.valueOf(entry.getValue());
         i++;
      
      }
   
      // Modelo para la table traces
      traces_dtm = new DefaultTableModel(data, columnNames);
      
      
      
      
      
      
      
      /**
      Se realiza proceso del algorithm
      **/
      
      
      /*
      System.out.println("\n\t ************3: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");
      WFG g1 = new WFG();
      g1.computeGraph(tracesList);
    
      System.out.println("\nPASO ******4: PREPROCESAMIENTO DEL GRAFO");
      double umbral =0.4; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
      g1.preProcesarGrafo(tracesList, BPMN, umbral);
    
      System.out.println("\nPASO ***********5: CONSTRUCCION DEL MODELO BPMN");
      g1.crearModeloBPMN(BPMN);
    
      LinkedHashMap<String,Integer> wfg = g1.WFG;
           
    // Se prepara la tabla Modelo
      columnNames = new String[]{"Key", "Value"};
      data = new String[g1.WFG.size()][2];
      i = 0;
      for (Map.Entry<String, Integer> entry : g1.WFG.entrySet()) {
         data[i][0] = "[" + entry.getKey() + "]";
         data[i][1] = String.valueOf(entry.getValue());
         i++;
      }
   
        // Model para la tabla Modelo
      model_dtm = new DefaultTableModel(data, columnNames);
      
      /*
      // Despliegue del modelo
      EstrategiaDeDespliegue edd = new EstrategiaDeDespliegue();
      deployment = (edd.mostrarDespliegue(g1.WFG));
      */
   
   }

   void refreshWindow() {
      // Se quitan los paneles del frame
      main_frm.remove(loadFile_pnl);
      main_frm.remove(menu_pnl);
      main_frm.remove(viewer_pnl);
   
      // Se construyen y revalidan los frames
      buildLoadFile();
      loadFile_pnl.validate();
      buildMenu();
      menu_pnl.validate();
      buildRaw();
      raw_pnl.validate();
      buildViewer();
      viewer_pnl.validate();
   
      // Se agregan los nuevos paneles al frame
      main_frm.add(loadFile_pnl, BorderLayout.NORTH);
      main_frm.add(menu_pnl, BorderLayout.WEST);
      main_frm.add(viewer_pnl, BorderLayout.CENTER);
   
      // Se revalida el frame
      SwingUtilities.updateComponentTreeUI(main_frm);
   
   }

}
