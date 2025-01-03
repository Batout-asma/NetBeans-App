/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.schoolman;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrator
 */
public class StudentInterface extends javax.swing.JFrame {

    static int userId;

    /**
     * Creates new form StudentInterface
     *
     * @param userId
     */
    public StudentInterface(int userId) {
        StudentInterface.userId = userId;
        initComponents(); // Initialize GUI components
        loadStudentData(); // Load student personal data
        loadNotesData(userId);
    }

    public void setUserId(int userId) {
        StudentInterface.userId = userId;
    }


    private void loadNotesData(int studentId) {
        final String query = "SELECT INTITULE_MODULE, NOTE FROM NOTE WHERE N_EL = ?";

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@localhost:1521:ORA19",
                        "School_admin",
                        "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

                    // Set the student ID parameter
                    stmt.setInt(1, studentId);

                    ResultSet rs = stmt.executeQuery();
                    DefaultTableModel model = (DefaultTableModel) notes_table.getModel();
                    model.setRowCount(0); // Clear existing rows

                    while (rs.next()) {
                        Object[] row = {
                            rs.getString("INTITULE_MODULE"), // Module name
                            rs.getDouble("NOTE") // Note value
                        };
                        model.addRow(row);
                    }

                    rs.close();
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "Error loading notes data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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

    private void loadStudentData() {
        final String query = "SELECT N_EL, NOM, PRENOM, DATEN FROM ELEVE WHERE N_EL = ?";

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@localhost:1521:ORA19",
                        "School_admin",
                        "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

                    // Set the student ID parameter
                    stmt.setInt(1, userId);

                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String id = String.valueOf(rs.getInt("N_EL"));
                        String firstname = rs.getString("PRENOM");
                        String lastname = rs.getString("NOM");
                        String birthdate = rs.getString("DATEN"); // Adjust format as needed

                        // Update UI components on the Event Dispatch Thread
                        SwingUtilities.invokeLater(() -> {
                            txt_id.setText(id);
                            txt_fname.setText(firstname);
                            txt_lname.setText(lastname);
                            jdc_bdate.setText(birthdate);
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

    /*
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
        noti_btn = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txt_id = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txt_fname = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txt_lname = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jdc_bdate = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        update_btn2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Student Dashboard", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 24))); // NOI18N

        notes_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Module", "Note"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(notes_table);

        noti_btn.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        noti_btn.setText("Notification");
        noti_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                noti_btnMouseClicked(evt);
            }
        });

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

        txt_fname.setEditable(false);
        txt_fname.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_fname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_fnameActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel4.setText("Lastname:");

        txt_lname.setEditable(false);
        txt_lname.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_lname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_lnameActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel5.setText("Birthdate:");

        jdc_bdate.setEditable(false);
        jdc_bdate.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jdc_bdate.setText("YYYY-MM-DD");
        jdc_bdate.setToolTipText("");
        jdc_bdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jdc_bdateActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Your Personal Data:");

        update_btn2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        update_btn2.setText("Logout");
        update_btn2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                update_btn2MouseClicked(evt);
            }
        });
        update_btn2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logout_btn(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(update_btn2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(46, 46, 46)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txt_lname)
                                    .addComponent(txt_id)
                                    .addComponent(txt_fname)
                                    .addComponent(jdc_bdate, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)))
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addContainerGap(12, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(noti_btn)
                        .addGap(14, 14, 14))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(noti_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txt_id, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_fname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_lname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jdc_bdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(update_btn2)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void jdc_bdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jdc_bdateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jdc_bdateActionPerformed

    private void update_btn2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_btn2MouseClicked

    }//GEN-LAST:event_update_btn2MouseClicked

    private void logout_btn(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logout_btn
        this.hide();
        new Login().show();
    }//GEN-LAST:event_logout_btn

    private void noti_btnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noti_btnMouseClicked
        new NotifList(userId).show();
    }//GEN-LAST:event_noti_btnMouseClicked

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
            java.util.logging.Logger.getLogger(StudentInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StudentInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StudentInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StudentInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new StudentInterface(userId).setVisible(true); // Example userId
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jdc_bdate;
    private javax.swing.JTable notes_table;
    private javax.swing.JButton noti_btn;
    private javax.swing.JTextField txt_fname;
    private javax.swing.JTextField txt_id;
    private javax.swing.JTextField txt_lname;
    private javax.swing.JButton update_btn2;
    // End of variables declaration//GEN-END:variables
}
