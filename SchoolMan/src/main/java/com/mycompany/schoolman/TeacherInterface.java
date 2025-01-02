/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.schoolman;

import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrator
 */
public class TeacherInterface extends javax.swing.JFrame {

    private static int userId;

    /**
     * Creates new form TeacherInterface
     *
     * @param userId
     */
    public TeacherInterface(int userId) {
        TeacherInterface.userId = userId;
        initComponents(); // Initialize GUI components
        loadTeacherData(); // Load student personal data
        loadNotesData();
        setupTableListener();
    }

    public void setUserId(int userId) {
        StudentInterface.userId = userId;
    }

    private void loadNotesData() {
        // Utilisation de l'opérateur || pour Oracle
        final String query = """
        SELECT 
            E.NOM || ' ' || E.PRENOM AS FULL_NAME, 
            E.N_EL, 
            N.NOTE 
        FROM NOTE N 
        JOIN ELEVE E ON N.N_EL = E.N_EL 
        WHERE N.N_ENS = ?
    """;

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@localhost:1521:ORA19",
                        "School_admin",
                        "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

                    // Paramètre pour l'enseignant
                    stmt.setInt(1, userId);

                    ResultSet rs = stmt.executeQuery();
                    DefaultTableModel model = (DefaultTableModel) notes_table.getModel();
                    model.setRowCount(0); // Effacer les lignes existantes

                    while (rs.next()) {
                        Object[] row = {
                            rs.getString("N_EL"), // Nom du module
                            rs.getString("FULL_NAME"), // NOM + PRENOM
                            rs.getDouble("NOTE") // Note
                        };
                        model.addRow(row);
                    }

                    rs.close();
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "Erreur lors du chargement des données : " + e.getMessage(), "Erreur Base de Données", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                // Actions après chargement
            }
        };
        worker.execute();
    }

    private void loadTeacherData() {
        final String query = "SELECT N_ENS, NOM, PRENOM, GRADE FROM ENSEIGNANT WHERE N_ENS = ?";

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@localhost:1521:ORA19",
                        "School_admin",
                        "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setInt(1, userId);

                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String id = String.valueOf(rs.getInt("N_ENS"));
                        String firstname = rs.getString("PRENOM");
                        String lastname = rs.getString("NOM");
                        String grade = rs.getString("GRADE");

                        // Update UI components on the Event Dispatch Thread
                        SwingUtilities.invokeLater(() -> {
                            txt_id.setText(id);
                            txt_fname.setText(firstname);
                            txt_lname.setText(lastname);
                            txt_grade.setText(grade);
                        });
                    }

                    rs.close();
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "Error loading personal data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                // Optionally handle any post-loading updates here
            }
        };
        worker.execute(); // Execute the background task
    }

    private void setupTableListener() {
        DefaultTableModel model = (DefaultTableModel) notes_table.getModel();

        model.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            // Check if the modified column is the one holding the notes (column index 2)
            if (column == 2) {
                String studentId = (String) model.getValueAt(row, 0); // Assuming N_EL (student ID) is in the 1st column
                String newNoteString = model.getValueAt(row, column).toString(); // Get the new NOTE value from the modified cell
                String newNote = String.valueOf(newNoteString);

                // Optionally, validate the new note input (if needed)
                updateNote(studentId, userId, newNote);
            }
        });
    }

    private void updateNote(String studentId, int teacherId, String newNote) {
        final String sql = "UPDATE NOTE SET NOTE = ? WHERE N_EL = ? AND N_ENS = ?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19",
                "School_admin",
                "admin"); PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Ensure the new note is numeric
            try {
                Double.valueOf(newNote);  // Check if the new note is a valid number
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid note. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;  // Stop if the note is invalid
            }

            // Set the query parameters
            stmt.setDouble(1, Double.parseDouble(newNote));  // Update note value as double
            stmt.setInt(2, Integer.parseInt(studentId));  // Set student ID as integer (N_EL)
            stmt.setInt(3, teacherId);  // Set teacher ID (N_ENS) as integer

            int rowsAffected = stmt.executeUpdate();

            // Provide feedback to the user
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Note updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to update the note. No matching record found.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating note: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchStudentData(String studentId) {
        final String query = "SELECT * FROM ELEVE WHERE N_EL = ?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19",
                "School_admin",
                "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, studentId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("NOM");
                String prenom = rs.getString("PRENOM");
                Date daten = rs.getDate("DATEN");

                // Show the data in a dialog or a new window
                JOptionPane.showMessageDialog(null, """
                                                    Student Data:
                                                    ID: """ + studentId + "\n"
                        + "Full Name: " + nom + " " + prenom + "\n"
                        + "Date Of Birth: " + daten,
                        "Student Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Student data not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching student data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddNoteDialog() {
        // Create and show the Add Note dialog
        JDialog addNoteDialog = new JDialog();
        addNoteDialog.setTitle("Add Note");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        // Add fields for nom, prenom, and note
        JTextField nomField = new JTextField();
        JTextField prenomField = new JTextField();
        JTextField noteField = new JTextField();

        panel.add(new JLabel("Nom:"));
        panel.add(nomField);
        panel.add(new JLabel("Prenom:"));
        panel.add(prenomField);
        panel.add(new JLabel("Note:"));
        panel.add(noteField);

        // Submit button for adding the note
        JButton submitBtn = new JButton("Add Note");
        submitBtn.addActionListener(submitEvent -> {
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String note = noteField.getText();

            // Validate inputs
            if (nom.isEmpty() || prenom.isEmpty() || note.isEmpty()) {
                JOptionPane.showMessageDialog(addNoteDialog, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Double.parseDouble(note);  // Check if note is a valid number
                insertNoteIntoDatabase(nom, prenom, note);  // Insert the note into the database
                addNoteDialog.dispose();  // Close the dialog after successful insertion
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addNoteDialog, "Invalid note. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(submitBtn);

        addNoteDialog.add(panel);
        addNoteDialog.pack();
        addNoteDialog.setLocationRelativeTo(null);
        addNoteDialog.setVisible(true);
    }

    private void insertNoteIntoDatabase(String nom, String prenom, String note) {
        // First, find the student's N_EL based on nom and prenom
        final String studentQuery = "SELECT N_EL FROM ELEVE WHERE NOM = ? AND PRENOM = ?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19",
                "School_admin",
                "admin"); PreparedStatement stmt = conn.prepareStatement(studentQuery)) {

            stmt.setString(1, nom);
            stmt.setString(2, prenom);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String studentId = rs.getString("N_EL"); // Get the student ID (N_EL)

                // Insert the note into the NOTE table
                insertNote(studentId, note);
            } else {
                JOptionPane.showMessageDialog(null, "Student not found with the provided name.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching student data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertNote(String studentId, String note) {
        final String insertQuery = "INSERT INTO NOTE (N_EL, N_ENS, INTITULE_MODULE, NOTE) VALUES (?, ?, ?, ?)";
        final String moduleQuery = "SELECT MODULE FROM ENSEIGNANT WHERE N_ENS = ?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19",
                "School_admin",
                "admin"); PreparedStatement stmt = conn.prepareStatement(insertQuery); PreparedStatement stmt2 = conn.prepareStatement(moduleQuery)) {

            // Get the module name based on the teacher's ID (userId)
            stmt2.setInt(1, userId);  // Set the teacher's ID (N_ENS)
            ResultSet rs = stmt2.executeQuery();

            String moduleName = null;
            if (rs.next()) {
                moduleName = rs.getString("MODULE");  // Get the module name from the query result
            } else {
                JOptionPane.showMessageDialog(null, "Module not found for the teacher.", "Error", JOptionPane.ERROR_MESSAGE);
                return;  // Exit if the module is not found
            }

            // Insert the new note into the NOTE table
            stmt.setString(1, studentId);  // Set student ID (N_EL)
            stmt.setInt(2, userId);  // Set teacher ID (N_ENS)
            stmt.setString(3, moduleName);  // Set the module name
            stmt.setDouble(4, Double.parseDouble(note));  // Set the note (convert to double)

            int rowsAffected = stmt.executeUpdate();  // Execute the insert query

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Note added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to add the note.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error inserting note: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        notes_table = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txt_id = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txt_fname = new javax.swing.JTextField();
        txt_lname = new javax.swing.JTextField();
        txt_grade = new javax.swing.JTextField();
        addNote_btn = new javax.swing.JButton();
        checkData_btn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Teacher Dashboard", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 24))); // NOI18N

        notes_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Full Name", "Note"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(notes_table);
        if (notes_table.getColumnModel().getColumnCount() > 0) {
            notes_table.getColumnModel().getColumn(0).setResizable(false);
            notes_table.getColumnModel().getColumn(1).setResizable(false);
            notes_table.getColumnModel().getColumn(2).setResizable(false);
        }

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Your Personal Data:");

        jLabel6.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel6.setText("IdNumber:");

        txt_id.setEditable(false);
        txt_id.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_id.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_idActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel3.setText("Firstname:");

        jLabel4.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel4.setText("Lastname:");

        jLabel5.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel5.setText("Grade:");

        txt_fname.setEditable(false);
        txt_fname.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_fname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_fnameActionPerformed(evt);
            }
        });

        txt_lname.setEditable(false);
        txt_lname.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_lname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_lnameActionPerformed(evt);
            }
        });

        txt_grade.setEditable(false);
        txt_grade.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_grade.setToolTipText("");
        txt_grade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_gradeActionPerformed(evt);
            }
        });

        addNote_btn.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        addNote_btn.setText("Add Note");
        addNote_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addNote_btnMouseClicked(evt);
            }
        });
        addNote_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNote_btnActionPerformed(evt);
            }
        });

        checkData_btn.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        checkData_btn.setText("Check Data");
        checkData_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checkData_btnMouseClicked(evt);
            }
        });
        checkData_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkData_btnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(addNote_btn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(checkData_btn))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txt_lname)
                                    .addComponent(txt_id)
                                    .addComponent(txt_fname)
                                    .addComponent(txt_grade, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addNote_btn)
                            .addComponent(checkData_btn))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_id, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_fname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_lname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_grade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txt_idActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_idActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_idActionPerformed

    private void txt_fnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_fnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_fnameActionPerformed

    private void txt_lnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_lnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_lnameActionPerformed

    private void txt_gradeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_gradeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_gradeActionPerformed

    private void addNote_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNote_btnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addNote_btnActionPerformed

    private void checkData_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkData_btnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkData_btnActionPerformed

    private void checkData_btnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkData_btnMouseClicked
        int selectedRow = notes_table.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) notes_table.getModel();
            String studentId = (String) model.getValueAt(selectedRow, 0); // Get N_EL

            // Query the database to fetch full student details based on N_EL
            fetchStudentData(studentId);
        } else {
            JOptionPane.showMessageDialog(null, "Please select a row to check the data.", "No Row Selected", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_checkData_btnMouseClicked

    private void addNote_btnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNote_btnMouseClicked
        openAddNoteDialog();
    }//GEN-LAST:event_addNote_btnMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TeacherInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TeacherInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TeacherInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TeacherInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TeacherInterface(userId).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNote_btn;
    private javax.swing.JButton checkData_btn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable notes_table;
    private javax.swing.JTextField txt_fname;
    private javax.swing.JTextField txt_grade;
    private javax.swing.JTextField txt_id;
    private javax.swing.JTextField txt_lname;
    // End of variables declaration//GEN-END:variables
}
