import SwiftUI

struct AddCertificate: View {
    @State var firstname = ""
    @State var lastname = ""
    @State var phonenumber = ""
    @State var email = ""
    @State var validto = Date().advanced(by: 86400)
    
    @State var selectedType = 0
    @State var selectedStatus = 0
    
    let types:[Int8] = CertificateType.allCases.map{ $0.rawValue }
    let status:[Int8] = CertificateStatus.allCases.map{ $0.rawValue }
    
    var isValid: Bool {
        firstname.count > 2 && lastname.count > 3 && phonenumber.count > 5
    }
    
    let onComplete: (String, String, String, String, CertificateType, CertificateStatus, Date) -> Void
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("addcert_firstname")) {
                    TextField("addcert_firstname_placeholder", text: $firstname)
                    TextField("addcert_lastname_placeholder", text: $lastname)
                }
                Section(header: Text("addcert_phonenumber")) {
                    TextField("addcert_phonenumber_placeholder", text: $phonenumber)
                    TextField("addcert_email_placeholder", text: $email)
                }
                Section(header: Text("addcert_type"))  {
                    Picker(selection: $selectedType, label: Text("Certification Type")) {
                        ForEach(types.indices) { i in
                            Text("certificatetype_\(types[i])".localized()).tag(i)
                        }
                    }.pickerStyle(SegmentedPickerStyle())
                }
                Section(header: Text("addcert_status"))  {
                    Picker(selection: $selectedStatus, label: Text("Certification Status")) {
                        ForEach(status.indices) { i in
                            Text("certificatestatus_\(status[i])".localized()).tag(i)
                        }
                    }.pickerStyle(SegmentedPickerStyle())
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
        onComplete(
            firstname,
            lastname,
            phonenumber,
            email,
            CertificateType(rawValue:types[selectedType])!,
            CertificateStatus(rawValue:status[selectedStatus])!,
            validto)
    }
}


struct AddCertificate_Previews: PreviewProvider {
    
    static var previews: some View {
        AddCertificate{ firstname, lastname, phonenumber, email, type, status, validto in
        }
    }
}
