package Swing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ParticipantApp extends JFrame {

    private JTextField tfNom, tfPrenom, tfEmail;
    private JLabel lblId, lblErreurEmail;
    private JButton btnAjouter, btnAfficher;
    private JTable table;
    private DefaultTableModel tableModel;
    

    private ArrayList<Participant> participants = new ArrayList<>();
    private int idCounter = 1;

    public ParticipantApp() {
        setTitle("Enregistrement des Participants");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
//        setResizable(false);
  
        // Panel de saisie
        JPanel panelSaisie = new JPanel(new GridLayout(8, 2, 5, 5));

        lblId = new JLabel("ID : " + idCounter);
        tfNom = new JTextField();
        tfPrenom = new JTextField();
        tfEmail = new JTextField();
        lblErreurEmail = new JLabel(" ");
        lblErreurEmail.setForeground(Color.RED);
        lblErreurEmail.setFont(new Font("Arial", Font.PLAIN, 11));
        
     // Barre de recherche
        JLabel lblRecherche = new JLabel("Recherche :");
        JTextField tfRecherche = new JTextField(20);
        panelSaisie.add(lblRecherche);
        panelSaisie.add(tfRecherche);


        panelSaisie.add(new JLabel("Identifiant :"));
        panelSaisie.add(lblId);
        panelSaisie.add(new JLabel("Nom :"));
        panelSaisie.add(tfNom);
        panelSaisie.add(new JLabel("Prénom :"));
        panelSaisie.add(tfPrenom);
        panelSaisie.add(new JLabel("Email :"));
        panelSaisie.add(tfEmail);
        panelSaisie.add(new JLabel("")); // vide
        panelSaisie.add(lblErreurEmail); // message sous email

        btnAjouter = new JButton("Ajouter");
        btnAfficher = new JButton("Afficher");
        btnAjouter.setEnabled(false);

        panelSaisie.add(btnAjouter);
        panelSaisie.add(btnAfficher);
        JButton btnExporter = new JButton("Exporter CSV");
        JButton btnImprimer = new JButton("Imprimer");
        panelSaisie.add(btnExporter);
        panelSaisie.add(btnImprimer);
        btnImprimer.addActionListener(e -> imprimerTable());
        btnExporter.addActionListener(e -> exporterCSV());

        
        // Table
        String[] colonnes = {"ID", "Nom", "Prénom", "Email"};
//        tableModel = new DefaultTableModel(colonnes, 0);
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Aucune cellule modifiable
            }
        };

        // activer la modification juste sur l'email
//        @Override
//        public boolean isCellEditable(int row, int column) {
//            return column == 3; // Email uniquement
//        }

        table = new JTable(tableModel);
        
        // pour mettre en place la recherche dynamique
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        tfRecherche.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtrer();
            }
            public void removeUpdate(DocumentEvent e) {
                filtrer();
            }
            public void changedUpdate(DocumentEvent e) {
                filtrer();
            }

            public void filtrer() {
                String texte = tfRecherche.getText().trim();
                if (texte.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texte)); // (?i) = ignore case
                }
            }
        });
        //end search


        // Layout principal
//        setLayout(new BorderLayout());
        add(panelSaisie, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Ajout des Listeners
        DocumentListener champListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { verifierChamps(); }
            public void removeUpdate(DocumentEvent e) { verifierChamps(); }
            public void changedUpdate(DocumentEvent e) { verifierChamps(); }
        };

        tfNom.getDocument().addDocumentListener(champListener);
        tfPrenom.getDocument().addDocumentListener(champListener);
        tfEmail.getDocument().addDocumentListener(champListener);
        tfEmail.addActionListener(e -> ajouterParticipant());

        // Actions des boutons
        btnAjouter.addActionListener(e -> ajouterParticipant());
        btnAfficher.addActionListener(e -> afficherParticipants());
        
        //Raccourci Clavier
        KeyStroke raccourciReset = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(raccourciReset, "resetForm");

        getRootPane().getActionMap().put("resetForm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tfNom.setText("");
                tfPrenom.setText("");
                tfEmail.setText("");
            }
        });

        
     // Menu contextuel
        JPopupMenu menu = new JPopupMenu();
        JMenuItem supprimerItem = new JMenuItem("Supprimer la ligne");
        menu.add(supprimerItem);

        // Ajout du menu à la table
        table.setComponentPopupMenu(menu);

        // Action suppression
        supprimerItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                participants.remove(row);
                tableModel.removeRow(row);
            }
        });

    }
    

    private boolean verifierChamp(JTextField champ) {
        String texte = champ.getText().trim();

        if (champ == tfEmail) {
            if (!texte.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                champ.setBackground(new Color(255, 200, 200));
                lblErreurEmail.setText("✖ Email invalide");
                return false;
            } else {
                champ.setBackground(Color.WHITE);
                lblErreurEmail.setText(" ");
                return true;
            }
        } else {
            if (texte.isEmpty()) {
                champ.setBackground(new Color(255, 200, 200));
                return false;
            } else {
                champ.setBackground(Color.WHITE);
                return true;
            }
        }
    }

    private void verifierChamps() {
        boolean champsValides = true;
        champsValides &= verifierChamp(tfNom);
        champsValides &= verifierChamp(tfPrenom);
        champsValides &= verifierChamp(tfEmail);

        btnAjouter.setEnabled(champsValides);
    }
    
    private void imprimerTable() {
        try {
            boolean complete = table.print(JTable.PrintMode.FIT_WIDTH, 
                                           new java.text.MessageFormat("Liste des Participants"), 
                                           new java.text.MessageFormat("Page - {0}"));
            if (complete) {
                JOptionPane.showMessageDialog(this, "Impression terminée !");
            } else {
                JOptionPane.showMessageDialog(this, "Impression annulée.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur d'impression : " + ex.getMessage());
        }
    }

    private void exporterCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer sous");

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            // Ajoute .csv si pas d'extension
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }

            try (java.io.PrintWriter writer = new java.io.PrintWriter(filePath)) {
                writer.println("ID,Nom,Prénom,Email");
                for (Participant p : participants) {
                    writer.printf("%d,%s,%s,%s%n", p.id, p.nom, p.prenom, p.email);
                }
                JOptionPane.showMessageDialog(this, "Exportation réussie !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'exportation : " + ex.getMessage());
            }
        }
    }


    private void ajouterParticipant() {
        String nom = tfNom.getText().trim();
        String prenom = tfPrenom.getText().trim();
        String email = tfEmail.getText().trim();

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            JOptionPane.showMessageDialog(this, "Email invalide !");
            return;
        }
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs doivent être remplis !");
            return;
        }
        
        for (Participant p : participants) {
            if (p.email.equalsIgnoreCase(email)) {
                JOptionPane.showMessageDialog(this, "Email déjà enregistré !");
                return;
            }
        }


        participants.add(new Participant(idCounter, nom, prenom, email));
        idCounter++;
        lblId.setText("ID: " + idCounter);

        tfNom.setText("");
        tfPrenom.setText("");
        tfEmail.setText("");

        verifierChamps(); // pour désactiver le bouton après ajout
    }

    private void afficherParticipants() {
        tableModel.setRowCount(0); // Réinitialiser la table
        for (Participant p : participants) {
            tableModel.addRow(new Object[]{p.id, p.nom, p.prenom, p.email});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParticipantApp().setVisible(true));
    }
}


