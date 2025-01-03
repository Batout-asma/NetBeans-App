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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Batout
 */
public class AdminInterface extends javax.swing.JFrame {

    private String role = null;

    /**
     * Creates new form AdminInterface
     */
    public AdminInterface() {

        initComponents();
        loadUserData(role);
    }

    private void loadUserData(String role) {
        final String query;
        if (role != null && !role.isEmpty()) {
            query = "SELECT * FROM LOGIN WHERE USER_ROLE = ?";  // Define the query with role filter
        } else {
            query = "SELECT * FROM LOGIN";  // Default query to fetch all data
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@localhost:1521:ORA19",
                        "School_admin",
                        "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

                    if (role != null && !role.isEmpty()) {
                        stmt.setString(1, role); // Set the role filter parameter
                    }

                    ResultSet rs = stmt.executeQuery();
                    DefaultTableModel model = (DefaultTableModel) users_table.getModel();
                    model.setRowCount(0); // Clear existing rows

                    while (rs.next()) {
                        Object[] row = {
                            rs.getInt("IDNO"),
                            rs.getString("USER_NAME"),
                            rs.getString("USER_PASS"),
                            rs.getString("VERIFIED")
                        };
                        model.addRow(row);
                    }

                    rs.close();
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "Error loading user data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
            }
        };
        worker.execute(); // Execute the background task
    }

    private void loadNotesData() {
        int selectedRow = users_table.getSelectedRow();
        if (selectedRow != -1) {
            int studentId = Integer.parseInt(users_table.getValueAt(selectedRow, 0).toString()); // Get IDNO from column 0
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
        } else {
            JOptionPane.showMessageDialog(null, "No student selected", "Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void populateFields(User user) {
        Date birthdate = user.getBirthdate();

        // Convert Date to String
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");  // Adjust the format as needed
        String birthdateStr = sdf.format(birthdate);

        // If you still want to set a String in a JDateChooser, use a custom implementation like this:
        txt_id.setText(String.valueOf(user.getId()));
        txt_uname.setText(user.getUsername());
        txt_upass.setText(user.getPassword());
        txt_fname.setText(user.getFirstname());
        txt_lname.setText(user.getLastname());

        // Ensure the birthdate is a Date object before calling setDate
        jdc_bdate.setText(birthdateStr); // This should work if getBirthdate() returns a Date object

        role_box.setSelectedItem(user.getUserRole());

    }

    private void loadUserDetails(int userId) {
        String userQuery = "SELECT L.IDNO, "
                + "CASE WHEN E.N_EL IS NOT NULL THEN E.NOM ELSE EN.NOM END AS NOM, "
                + "CASE WHEN E.N_EL IS NOT NULL THEN E.PRENOM ELSE EN.PRENOM END AS PRENOM, "
                + "CASE WHEN E.N_EL IS NOT NULL THEN TO_CHAR(E.DATEN, 'YYYY-MM-DD') ELSE EN.GRADE END AS GRADE, "
                + "CASE WHEN E.N_EL IS NOT NULL THEN NULL ELSE TO_CHAR(EN.SALAIRE) END AS SALAIRE, "
                + "L.USER_NAME, L.USER_PASS, L.USER_ROLE "
                + "FROM LOGIN L "
                + "LEFT JOIN ELEVE E ON L.IDNO = E.N_EL "
                + "LEFT JOIN ENSEIGNANT EN ON L.IDNO = EN.N_ENS "
                + "WHERE L.IDNO = ?";  // Parameterized query

        String moduleQuery = "SELECT DISTINCT INTITULE_MODULE FROM NOTE WHERE N_ENS = ?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19", "School_admin", "admin"); PreparedStatement userStmt = conn.prepareStatement(userQuery); PreparedStatement moduleStmt = conn.prepareStatement(moduleQuery)) {

            // Set the userId parameter in the query
            userStmt.setInt(1, userId);
            try (ResultSet userRs = userStmt.executeQuery()) {
                if (userRs.next()) {
                    // Set field values
                    txt_id.setText(String.valueOf(userRs.getInt("IDNO"))); // IDNO
                    txt_lname.setText(userRs.getString("NOM"));                 // Last Name
                    txt_fname.setText(userRs.getString("PRENOM"));              // First Name
                    txt_uname.setText(userRs.getString("USER_NAME"));           // Username
                    txt_upass.setText(userRs.getString("USER_PASS"));           // Password
                    role_box.setSelectedItem(userRs.getString("USER_ROLE")); // User Role

                    // Determine if the user is a teacher or student/admin
                    String gradeOrDate = userRs.getString("GRADE");
                    if (gradeOrDate == null) { // Matches a date format
                        jLabel5.setText("Birthdate:"); // User is an admin
                    } else {
                        if (gradeOrDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            jLabel5.setText("Birthdate:"); // User is a student
                        } else {
                            jLabel5.setText("Grade"); // User is a teacher
                        }
                    }
                    jdc_bdate.setText(gradeOrDate != null ? gradeOrDate : "");

                    // Fetch and set module if the user is a teacher
                    if ("Teacher".equalsIgnoreCase(userRs.getString("USER_ROLE"))) {
                        jLabel10.setEnabled(true);
                        moduleStmt.setInt(1, userId);
                        try (ResultSet moduleRs = moduleStmt.executeQuery()) {
                            if (moduleRs.next()) {
                                String moduleName = moduleRs.getString("INTITULE_MODULE");
                                txt_module.setText(moduleName); // Display module in text field
                            } else {
                                txt_module.setText("No module found"); // No module assigned
                            }
                            // Close ResultSet for module query
                        }
                    } else {
                        jLabel10.setEnabled(false);
                        txt_module.setText(""); // Clear module text field for non-teachers
                    }
                } else {
                    // Handle case when no user is found for the given userId
                    JOptionPane.showMessageDialog(null, "No user found with ID: " + userId, "Not Found", JOptionPane.ERROR_MESSAGE);
                }
                // Close ResultSet for user query
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while fetching user data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        students_tog = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        users_table = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        txt_id = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txt_fname = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txt_lname = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jdc_bdate = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        role_box = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        txt_uname = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txt_upass = new javax.swing.JPasswordField();
        update_btn = new javax.swing.JButton();
        verify_btn = new javax.swing.JButton();
        teachers_tog = new javax.swing.JToggleButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        notes_table = new javax.swing.JTable();
        txt_module = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        update_btn1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Admin Dashboard", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Rockwell", 1, 24))); // NOI18N

        students_tog.setFont(new java.awt.Font("Rockwell", 1, 18)); // NOI18N
        students_tog.setText("Students List");
        students_tog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                students_togActionPerformed(evt);
            }
        });

        users_table.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        users_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Username", "Password", "Verified"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        users_table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                users_tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(users_table);

        jLabel6.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel6.setText("IdNumber:");

        txt_id.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_id.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_idActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel3.setText("Firstname:");

        txt_fname.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_fname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_fnameActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel4.setText("Lastname:");

        txt_lname.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_lname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_lnameActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel5.setText("Birthdate:");

        jdc_bdate.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jdc_bdate.setText("YYYY-MM-DD");
        jdc_bdate.setToolTipText("");
        jdc_bdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jdc_bdateActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel7.setText("UserRole:");

        role_box.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        role_box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Unkown", "Student", "Teacher", "Admin" }));

        jLabel1.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel1.setText("Username:");

        txt_uname.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_uname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_unameActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel2.setText("Password:");

        txt_upass.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N

        update_btn.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        update_btn.setText("Update");
        update_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                update_btnMouseClicked(evt);
            }
        });

        verify_btn.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        verify_btn.setText("Verify");
        verify_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                verify_btnMouseClicked(evt);
            }
        });
        verify_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verify_btnActionPerformed(evt);
            }
        });

        teachers_tog.setFont(new java.awt.Font("Rockwell", 1, 18)); // NOI18N
        teachers_tog.setText("Teachers List");
        teachers_tog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teachers_togActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Users Table:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Notes Table:");

        notes_table.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
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
        notes_table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                notes_tableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(notes_table);

        txt_module.setEditable(false);
        txt_module.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        txt_module.setEnabled(false);
        txt_module.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_moduleActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Rockwell", 1, 14)); // NOI18N
        jLabel10.setText("Module:");
        jLabel10.setAutoscrolls(true);
        jLabel10.setEnabled(false);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel11.setText("Filter:");

        update_btn1.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        update_btn1.setText("Logout");
        update_btn1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                update_btn1MouseClicked(evt);
            }
        });
        update_btn1.addActionListener(new java.awt.event.ActionListener() {
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
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(update_btn1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(242, 242, 242)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_lname, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txt_fname)
                            .addComponent(jdc_bdate)
                            .addComponent(txt_uname)
                            .addComponent(txt_upass, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(role_box, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txt_id)
                            .addComponent(txt_module)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(teachers_tog)
                                .addGap(18, 18, 18)
                                .addComponent(students_tog))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(update_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(verify_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(students_tog)
                            .addComponent(teachers_tog)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_id, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_uname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_upass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(13, 13, 13)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_fname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_lname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jdc_bdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(role_box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_module, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(verify_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(update_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(update_btn1)
                        .addGap(14, 14, 14)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(29, 29, 29)
                        .addComponent(jLabel9)
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
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

    private void students_togActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_students_togActionPerformed
        if (students_tog.isSelected()) {
            teachers_tog.setSelected(false); // Deselect teacher toggle
            role = "Student"; // Set the role to "Student"
            loadUserData(role); // Reload data with "Student" filter
        } else {
            role = null; // Reset the role (no filter)
            loadUserData(role); // Load all data
        }
    }//GEN-LAST:event_students_togActionPerformed

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

    private void txt_unameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_unameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_unameActionPerformed

    private void update_btnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_btnMouseClicked
// Retrieve updated data from the form fields
        int id = Integer.parseInt(txt_id.getText());
        String lastName = txt_lname.getText();
        String firstName = txt_fname.getText();
        String username = txt_uname.getText();
        String password = txt_upass.getText();
        String gradeOrDate = jdc_bdate.getText();
        String role = role_box.getSelectedItem().toString();

        // Queries for updating LOGIN and role-specific tables
        String loginQuery = "UPDATE LOGIN SET USER_NAME = ?, USER_PASS = ? WHERE IDNO = ?";
        String roleSpecificQuery;

        if (role.equalsIgnoreCase("Student")) {
            // Query to update a student's data
            roleSpecificQuery = "UPDATE ELEVE SET NOM = ?, PRENOM = ?, DATEN = TO_DATE(?, 'YYYY-MM-DD') WHERE N_EL = ?";
        } else if (role.equalsIgnoreCase("Teacher")) {
            // Query to update a teacher's data without SALAIRE
            roleSpecificQuery = "UPDATE ENSEIGNANT SET NOM = ?, PRENOM = ?, GRADE = ? WHERE N_ENS = ?";
        } else {
            JOptionPane.showMessageDialog(this, "Invalid role selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19", "School_admin", "admin"); PreparedStatement loginStmt = conn.prepareStatement(loginQuery); PreparedStatement roleStmt = conn.prepareStatement(roleSpecificQuery)) {

            // Update LOGIN table
            loginStmt.setString(1, username);
            loginStmt.setString(2, password);
            loginStmt.setInt(3, id);
            loginStmt.executeUpdate();

            // Update role-specific table
            if (role.equalsIgnoreCase("Student")) {
                roleStmt.setString(1, lastName);
                roleStmt.setString(2, firstName);
                roleStmt.setString(3, gradeOrDate); // Birthdate
                roleStmt.setInt(4, id);            // Student ID
            } else if (role.equalsIgnoreCase("Teacher")) {
                roleStmt.setString(1, lastName);
                roleStmt.setString(2, firstName);
                roleStmt.setString(3, gradeOrDate); // Grade
                roleStmt.setInt(4, id);              // Teacher ID
            }

            int rowsAffected = roleStmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUserData(role); // Refresh the users_table
            } else {
                JOptionPane.showMessageDialog(this, "No record found to update.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating record: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_update_btnMouseClicked

    private void verify_btnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_verify_btnMouseClicked
        // Get the selected row index from the users_table
        int selectedRow = users_table.getSelectedRow();

        if (selectedRow == -1) {
            // No row is selected
            JOptionPane.showMessageDialog(this, "Please select a user to verify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Retrieve the ID of the selected user from the table
        int userId = Integer.parseInt(users_table.getValueAt(selectedRow, 0).toString()); // Assuming ID is in the first column

        // Query to update the VERIFIED field in the database
        String query = "UPDATE LOGIN SET VERIFIED = 'Y' WHERE IDNO = ?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19", "School_admin", "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the ID parameter in the query
            stmt.setInt(1, userId);

            // Execute the update
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "User verified successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUserData(role); // Refresh the users_table to show updated data
            } else {
                JOptionPane.showMessageDialog(this, "No record found to verify.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error verifying user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_verify_btnMouseClicked

    private void verify_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verify_btnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_verify_btnActionPerformed

    private void users_tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_users_tableMouseClicked
        // Get the selected row index
        int selectedRow = users_table.getSelectedRow();

        // Check if a row is actually selected
        if (selectedRow != -1) {
            // Assuming the ID is in the first column (index 0)
            int userId = (int) users_table.getValueAt(selectedRow, 0);

            // Load the user details based on the selected user ID
            loadUserDetails(userId);
            String role = getUserRole(userId);  // You need to implement this function to get the role of the selected user

            if ("student".equalsIgnoreCase(role)) {
                loadNotesData();
            } else {
                clearNotesTable();
            }

        }
    }//GEN-LAST:event_users_tableMouseClicked
    private String getUserRole(int userId) {
        String role = "";
        String query = "SELECT USER_ROLE FROM LOGIN WHERE IDNO = ?";  // Assuming 'LOGIN' table holds the user role

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:ORA19", "School_admin", "admin"); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId); // Set the userId parameter
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("USER_ROLE");  // Retrieve the role
                }
            }

        } catch (SQLException e) {
        }

        return role;
    }

    private void clearNotesTable() {
        DefaultTableModel notesModel = (DefaultTableModel) notes_table.getModel();
        notesModel.setRowCount(0);  // Clear all rows in notes table
    }

    private void teachers_togActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teachers_togActionPerformed
        if (teachers_tog.isSelected()) {
            jLabel10.setEnabled(true);
            students_tog.setSelected(false); // Deselect teacher toggle
            role = "Teacher"; // Set the role to "Teacher"
            loadUserData(role); // Reload data with "Teacher" filter
        } else {
            jLabel10.setEnabled(false);
            role = null; // Reset the role (no filter)
            loadUserData(role); // Load all data
        }
    }//GEN-LAST:event_teachers_togActionPerformed

    private void notes_tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_notes_tableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_notes_tableMouseClicked

    private void txt_moduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_moduleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_moduleActionPerformed

    private void update_btn1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_update_btn1MouseClicked
        this.hide();
        new Login().show();
    }//GEN-LAST:event_update_btn1MouseClicked

    private void logout_btn(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logout_btn
        // TODO add your handling code here:
    }//GEN-LAST:event_logout_btn

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
            java.util.logging.Logger.getLogger(AdminInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminInterface().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jdc_bdate;
    private javax.swing.JTable notes_table;
    private javax.swing.JComboBox<String> role_box;
    private javax.swing.JToggleButton students_tog;
    private javax.swing.JToggleButton teachers_tog;
    private javax.swing.JTextField txt_fname;
    private javax.swing.JTextField txt_id;
    private javax.swing.JTextField txt_lname;
    private javax.swing.JTextField txt_module;
    private javax.swing.JTextField txt_uname;
    private javax.swing.JPasswordField txt_upass;
    private javax.swing.JButton update_btn;
    private javax.swing.JButton update_btn1;
    private javax.swing.JTable users_table;
    private javax.swing.JButton verify_btn;
    // End of variables declaration//GEN-END:variables
}
