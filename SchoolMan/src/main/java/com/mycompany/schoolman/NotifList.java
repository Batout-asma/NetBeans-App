/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.schoolman;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author Administrator
 */
public class NotifList extends javax.swing.JFrame {

    static int userId;

    /**
     * Creates new form NotifList
     * @param userId
     */
    public NotifList(int userId) {
        NotifList.userId = userId;
        initComponents();
        loadNotifications(userId);
    }

    private void loadNotifications(int userId) {
        SwingWorker worker = new SwingWorker() {
            private final java.util.List<Notification> notifications = new java.util.ArrayList<>();

            @Override
            protected Object doInBackground() throws Exception {
                final String query = """
    SELECT 
        E.PRENOM || ' ' || E.NOM AS FULL_NAME, 
        N.N_ENS, 
        N.INTITULE_MODULE,
        N.N_EL  -- Add N_EL here as part of the unique identifier
    FROM NOTE N
    JOIN ENSEIGNANT E ON N.N_ENS = E.N_ENS
    WHERE N.N_EL = ? AND N.SEEN = 0
""";

                try (Connection conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@localhost:1521:ORA19",
                        "School_admin",
                        "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setInt(1, userId); // Set student ID parameter
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        String teacherName = rs.getString("FULL_NAME");
                        String module = rs.getString("INTITULE_MODULE");
                        String noteEl = rs.getString("N_EL");
                        String noteEns = rs.getString("N_ENS");
                        String message = teacherName + " updated the note for " + module;

                        // Combine N_EL and N_ENS to form a composite identifier
                        String compositeId = noteEl + "_" + noteEns;
                        notifications.add(new Notification(message, compositeId));
                    }
                    rs.close();
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "Error loading notifications: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                updateNotificationList(notifications);
            }
        };
        worker.execute();
    }

    private void updateNotificationList(java.util.List<Notification> notifications) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Notification notification : notifications) {
            listModel.addElement(notification.getMessage());
        }
        jList1.setModel(listModel);

        // Add MouseListener to delete notification when clicked
        jList1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = jList1.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Notification selectedNotification = notifications.get(index);
                    deleteNotification(selectedNotification);
                }
            }
        });
    }

    private void deleteNotification(Notification notification) {
        // Delete the notification from the JList
        DefaultListModel<String> model = (DefaultListModel<String>) jList1.getModel();
        model.removeElement(notification.getMessage()); // Remove based on message

        // Set SEEN = 1 for this notification in the database
        String updateQuery = "UPDATE NOTE SET SEEN = 1 WHERE N_EL = ? AND N_ENS = ?";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19", "School_admin", "admin"); PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            // Use the compositeId (N_EL and N_ENS) from the notification object
            String[] ids = notification.getNoteId().split("_"); // Split compositeId into N_EL and N_ENS
            String nEl = ids[0];  // Student ID (N_EL)
            String nEns = ids[1]; // Teacher ID (N_ENS)

            stmt.setString(1, nEl); // Set N_EL in the query
            stmt.setString(2, nEns); // Set N_ENS in the query
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating SEEN status: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        cancel_btn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(jList1);

        cancel_btn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        cancel_btn.setText("Cancel");
        cancel_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cancel_btnMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancel_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancel_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void cancel_btnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancel_btnMouseClicked
        this.hide();
    }//GEN-LAST:event_cancel_btnMouseClicked

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
            java.util.logging.Logger.getLogger(NotifList.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NotifList.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NotifList.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NotifList.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new NotifList(userId).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel_btn;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
