/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.schoolman;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 *
 * @author Batout
 */
public class SchoolMan {

    public static void main(String[] args) {
        try{
            Class.forName("oracle.jdbc.OracleDriver");
            String url = "jdbc:oracle:thin:@localhost:1521:ORA19";
            String uname = "SYS AS SYSDBA";
            String upass = "2004";
            Connection conn = DriverManager.getConnection(url, uname, upass);
            String sql = "SELECT * FROM USERLOGIN";
            PreparedStatement pst = conn.prepareStatement (sql);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                System.out.println("FIRSTNAME" + "\t" + "LASTNAME");
                System.out.println(rs.getString(3) + "\t\t" + rs.getString(2));
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
        }
    }
}
