module com.example.tifreader_asn1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.tifreader_asn1 to javafx.fxml;
    exports com.example.tifreader_asn1;
}