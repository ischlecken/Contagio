import SwiftUI

struct AddCertificate: View {
    @State var firstname = ""
    @State var lastname = ""
    @State var phonenumber = ""
    @State var email = ""
    @State var validto = Date().advanced(by: 86400)
    
    var isValid: Bool {
        firstname.count > 2 && lastname.count > 3 && phonenumber.count > 5
    }
    
    let onComplete: (String, String,String,String, Date) -> Void
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("addcert_firstname")) {
                    TextField("addcert_firstname_placeholder", text: $firstname)
                }
                Section(header: Text("addcert_lastname")) {
                    TextField("addcert_lastname_placeholder", text: $lastname)
                }
                Section(header: Text("addcert_phonenumber")) {
                    TextField("addcert_phonenumber_placeholder", text: $phonenumber)
                }
                Section(header: Text("addcert_email")) {
                    TextField("addcert_email_placeholder", text: $email)
                }
                Section {
                    DatePicker(selection: $validto,displayedComponents: [.hourAndMinute, .date]) {
                        Text("addcert_validto").foregroundColor(Color(.gray))
                    }
                }
                Section {
                    Button(action: addCertificateAction) {
                        Text("addcert_addbutton")
                    }
                    .disabled(!isValid)
                }
            }
            .navigationBarTitle(Text("addcert_title"), displayMode: .inline)
        }
    }
    
    private func addCertificateAction() {
        onComplete(firstname,lastname,phonenumber,email,validto)
    }
}


struct AddCertificate_Previews: PreviewProvider {
    
    static var previews: some View {
        AddCertificate{ firstname, lastname, phonenumber,email,validto in
        }
    }
}
