import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Application {
    private JFrame frame;
    private JList<Produit> produitsList;
    private JPanel repasPanel;
    private DefaultListModel<Produit> produitsModel;
    private DefaultListModel<Repas> repasModel;
    private ProduitModel produitModel;
    private RepasModel repasListModel;
    private Calendar calendrier;

    public Application() {
        frame = new JFrame("Gestion des Produits et Repas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        produitModel = new ProduitModel();
        repasListModel = new RepasModel();
        produitsModel = new DefaultListModel<>();
        produitsList = new JList<>(produitsModel);
        repasModel = new DefaultListModel<>();
        repasPanel = new JPanel(new GridLayout(2, 7));

        JButton ajouterProduitButton = new JButton("Ajouter Produit");
        JButton planifierRepasButton = new JButton("Planifier Repas");

        ajouterProduitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ajouterProduit();
            }
        });

        planifierRepasButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                planifierRepas();
            }
        });

        frame.setLayout(new BorderLayout());

        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(new JLabel("Liste des Produits"), BorderLayout.NORTH);
        panel1.add(new JScrollPane(produitsList), BorderLayout.CENTER);
        panel1.add(ajouterProduitButton, BorderLayout.SOUTH);

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(new JLabel("Calendrier des Repas"), BorderLayout.NORTH);
        panel2.add(repasPanel, BorderLayout.CENTER);
        panel2.add(planifierRepasButton, BorderLayout.SOUTH);

        frame.add(panel1, BorderLayout.WEST);
        frame.add(panel2, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public void ajouterProduit() {
        String nom = JOptionPane.showInputDialog(frame, "Nom du produit : ");
        if (nom != null && !nom.trim().isEmpty()) {
            String datePeremption = JOptionPane.showInputDialog(frame, "Date de péremption (jj-mm-aaaa) : ");
            String dateAchat = JOptionPane.showInputDialog(frame, "Date d'achat (jj-mm-aaaa) : ");

            String[] typesQuantite = { "Poids (kg, g)", "Volume (L, cl, ml)", "Unité (1, 2, 3...)" };
            int choixQuantite = JOptionPane.showOptionDialog(
                    frame,
                    "Choisissez le type de quantité :",
                    "Type de Quantité",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    typesQuantite,
                    typesQuantite[0]);

            double quantite = 0;
            TypeQuantite typeQuantite = null;

            switch (choixQuantite) {
                case 0: // Poids
                    String[] unitesPoids = { "kg", "g" };
                    String unitePoids = (String) JOptionPane.showInputDialog(
                            frame,
                            "Choisissez l'unité de poids :",
                            "Unité de Poids",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            unitesPoids,
                            unitesPoids[0]);

                    quantite = Double
                            .parseDouble(JOptionPane.showInputDialog(frame, "Quantité en " + unitePoids + " : "));
                    typeQuantite = TypeQuantite.POIDS;
                    break;

                case 1: // Volume
                    String[] unitesVolume = { "L", "cl", "ml" };
                    String uniteVolume = (String) JOptionPane.showInputDialog(
                            frame,
                            "Choisissez l'unité de volume :",
                            "Unité de Volume",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            unitesVolume,
                            unitesVolume[0]);

                    quantite = Double
                            .parseDouble(JOptionPane.showInputDialog(frame, "Quantité en " + uniteVolume + " : "));
                    typeQuantite = TypeQuantite.VOLUME;
                    break;

                case 2: // Unité
                    quantite = Double.parseDouble(JOptionPane.showInputDialog(frame, "Quantité en unité : "));
                    typeQuantite = TypeQuantite.UNITE;
                    break;
            }

            Produit produit = new Produit(nom, datePeremption, dateAchat, quantite, typeQuantite);
            produitModel.ajouterProduit(produit);
            produitsModel.addElement(produit);

            trierProduits();
        } else {
            JOptionPane.showMessageDialog(frame, "Nom de produit invalide.");
        }
    }

    public void planifierRepas() {
        int index = produitsList.getSelectedIndex();
        if (index >= 0) {
            Produit produit = produitsModel.get(index);

            int option = JOptionPane.showOptionDialog(
                    frame,
                    "Sélectionnez le jour et le moment du repas :",
                    "Planification du Repas",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[] { "Midi", "Soir" },
                    "Midi");

            if (option == JOptionPane.YES_OPTION || option == JOptionPane.NO_OPTION) {
                int jour = choisirJour();
                if (jour != -1) {
                    MomentRepas moment = (option == JOptionPane.YES_OPTION) ? MomentRepas.MIDI : MomentRepas.SOIR;
                    Repas repas = new Repas(produit.getNom(), getDatePlusJours(jour - 1), moment, produit,
                            produit.getQuantite(), produit.getTypeQuantite());
                    repasListModel.ajouterRepas(repas);
                    produitModel.retirerProduit(produit);
                    produitsModel.remove(index);
                    afficherRepas();
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Sélectionnez d'abord un produit.");
        }
    }

    private int choisirJour() {
        String[] joursSemaine = {
                "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"
        };

        int choix = JOptionPane.showOptionDialog(
                frame,
                "Choisissez le jour de la semaine :",
                "Choix du Jour",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                joursSemaine,
                joursSemaine[0]);

        if (choix != JOptionPane.CLOSED_OPTION) {
            // Le choix de l'utilisateur commence à 0 (Dimanche), donc nous ajoutons 1 pour
            // obtenir le jour correct.
            return choix + 1;
        }

        return -1; // Si l'utilisateur a annulé
    }

    private void trierProduits() {
        List<Produit> produits = produitModel.getProduits();
        Collections.sort(produits, (p1, p2) -> p1.getDatePeremption().compareTo(p2.getDatePeremption()));
        produitsModel.clear();
        for (Produit produit : produits) {
            produitsModel.addElement(produit);
        }
    }

    private void afficherRepas() {
        repasPanel.removeAll();
        for (int i = 0; i < 7; i++) {
            JPanel jourPanel = new JPanel();
            jourPanel.setBorder(BorderFactory.createTitledBorder(
                    new SimpleDateFormat("EEEE").format(getDatePlusJours(i))));

            for (MomentRepas moment : MomentRepas.values()) {
                JPanel repasMomentPanel = new JPanel(new GridLayout(0, 1));

                for (Repas repas : repasListModel.getRepas()) {
                    if (getJourSemaine(repas.getDate()) == i && repas.getMoment() == moment) {
                        repasMomentPanel.add(new JLabel(
                                repas.getNom() + " (" + repas.getQuantite() + " " + repas.getTypeQuantite() + ")"));
                    }
                }

                jourPanel.add(repasMomentPanel);
            }

            repasPanel.add(jourPanel);
        }
        repasPanel.revalidate();
        repasPanel.repaint();
    }

    private int getJourSemaine(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    private Date getDatePlusJours(int jours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, jours);
        return calendar.getTime();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Application();
            }
        });
    }
}

// ... Les autres classes restent inchangées

enum MomentRepas {
    MIDI,
    SOIR
}

class Produit {
    private String nom;
    private Date datePeremption;
    private Date dateAchat;
    private double quantite;
    private TypeQuantite typeQuantite;

    public Produit(String nom, String datePeremption, String dateAchat, double quantite, TypeQuantite typeQuantite) {
        this.nom = nom;
        try {
            this.datePeremption = new SimpleDateFormat("dd/MM/yyyy").parse(datePeremption);
            this.dateAchat = new SimpleDateFormat("dd/MM/yyyy").parse(dateAchat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.quantite = quantite;
        this.typeQuantite = typeQuantite;
    }

    public String getNom() {
        return nom;
    }

    public Date getDatePeremption() {
        return datePeremption;
    }

    public Date getDateAchat() {
        return dateAchat;
    }

    public double getQuantite() {
        return quantite;
    }

    public TypeQuantite getTypeQuantite() {
        return typeQuantite;
    }

    @Override
    public String toString() {
        String unite = "";
        switch (typeQuantite) {
            case POIDS:
                if (quantite >= 1) {
                    unite = "kg";
                } else {
                    quantite *= 1000; // Convertir en grammes
                    unite = "g";
                }
                break;
            case VOLUME:
                if (quantite >= 1) {
                    unite = "L";
                } else if (quantite >= 0.1) {
                    quantite *= 100; // Convertir en centilitres
                    unite = "cl";
                } else {
                    quantite *= 1000; // Convertir en millilitres
                    unite = "ml";
                }
                break;
            case UNITE:
                unite = (quantite > 1) ? "unités" : "unité";
                break;
        }

        return nom + " (DLC : " + new SimpleDateFormat("dd/MM/yyyy").format(datePeremption) +
                ", Qté : " + quantite + " " + unite + ")" +
                ", Achat : " + new SimpleDateFormat("dd/MM/yyyy").format(dateAchat);
    }
}

class Repas {
    private String nom;
    private Date date;
    private MomentRepas moment;
    private Produit produit;
    private double quantite;
    private TypeQuantite typeQuantite;

    public Repas(String nom, Date date, MomentRepas moment, Produit produit, double quantite,
            TypeQuantite typeQuantite) {
        this.nom = nom;
        this.date = date;
        this.moment = moment;
        this.produit = produit;
        this.quantite = quantite;
        this.typeQuantite = typeQuantite;
    }

    public String getNom() {
        return nom;
    }

    public Date getDate() {
        return date;
    }

    public MomentRepas getMoment() {
        return moment;
    }

    public Produit getProduit() {
        return produit;
    }

    public double getQuantite() {
        return quantite;
    }

    public TypeQuantite getTypeQuantite() {
        return typeQuantite;
    }

    @Override
    public String toString() {
        return nom + " (Date : " + new SimpleDateFormat("dd/MM/yyyy").format(date) +
                ", Moment : " + moment +
                ", Quantité : " + quantite + " " + typeQuantite + ")";
    }
}

class ProduitModel {
    private List<Produit> produits;

    public ProduitModel() {
        produits = new ArrayList<>();
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void ajouterProduit(Produit produit) {
        produits.add(produit);
    }

    public void retirerProduit(Produit produit) {
        produits.remove(produit);
    }
}

class RepasModel {
    private List<Repas> repas;

    public RepasModel() {
        repas = new ArrayList<>();
    }

    public List<Repas> getRepas() {
        return repas;
    }

    public void ajouterRepas(Repas repas) {
        this.repas.add(repas);
    }
}

enum TypeQuantite {
    POIDS,
    VOLUME,
    UNITE
}
