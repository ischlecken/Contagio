import SwiftUI

struct ModifyCertificate: View {
    
    @Environment(\.presentationMode) var presentation
    @State var certificate: Certificate
    @State var certifcatePhoto: UIImage
    @State var selectedStatus: Int
    @State var showCert = false
    @State var showAlert = false
    @State var validuntil = Date()
    
    let types:[Int8] = CertificateType.allCases.map{ $0.rawValue }
    let status:[Int8] = CertificateStatus.allCases.map{ $0.rawValue }
    let onChange: (ModifyCertificateResponse) -> Void
    
    var body: some View {
        let statusColor = Color("backgroundcertificatestatus_\(selectedStatus)")
        
        NavigationView {
            List {
                Section() {
                    HStack {
                        Image(uiImage:certifcatePhoto)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(height: 120, alignment: .top)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(statusColor, lineWidth: 4))
                        Spacer()
                        VStack(alignment: .trailing) {
                            Text("\(certificate.firstname!) \(certificate.lastname!)").font(.largeTitle)
                            Spacer()
                            Text("certificatetype_\(certificate.type)".localized()).font(.callout).bold()
                            
                            HStack {
                                Text("addcert_validto").foregroundColor(Color(.gray)).font(.caption)
                                Text(DateFormatter.certificate.string(from: certificate.validuntil!)).font(.caption).bold()
                            }
                        }
                        
                    }
                }
                Section()  {
                    VStack(alignment: .leading) {
                        Text("addcert_status").foregroundColor(Color(.gray)).font(.caption)
                        Picker(selection: $selectedStatus, label: Text("Certification Status")) {
                            ForEach(status.indices) { i in
                                Text("certificatestatus_\(status[i])".localized()).tag(i)
                            }
                        }.pickerStyle(SegmentedPickerStyle())
                    }
                }
                Section() {
                    VStack(alignment: .leading) {
                        Text("addcert_phonenumber").foregroundColor(Color(.gray)).font(.caption)
                        Text(certificate.phonenumber!)
                    }
                    VStack(alignment: .leading) {
                        Text("addcert_email").foregroundColor(Color(.gray)).font(.caption)
                        Text(certificate.email!)
                    }
                }
                Section {
                    DatePicker(selection: $validuntil, displayedComponents: [.hourAndMinute, .date]) {
                        Text("addcert_validto").foregroundColor(Color(.gray))
                    }
                }
                Section {
                    Button(action: modifyCertificateAction) {
                        Text("modifycert_updatebutton")
                    }
                }
                Section {
                    Button(action: { showAlert.toggle()}) {
                        Text("modifycert_deletebutton")
                    }
                }
                
                Section {
                    Button(action: { showCert.toggle()}) {
                        Text("modifycert_showpass")
                    }
                }
            }
            .listStyle(InsetGroupedListStyle())
            .sheet(isPresented: $showCert) {
                PassView(pass: (UIApplication.shared.delegate as!AppDelegate).eventPass!)
            }
            .alert(isPresented: $showAlert) {
                Alert(title:Text("alert_deletecertificate_title"),
                      message: Text("alert_deletecertificate_message"),
                      primaryButton: .destructive(Text("alert_deletecertificate_deletebutton"),action: deleteCertificateAction),
                      secondaryButton: .cancel()
                )
            }
            
        }
    }
    
    private func modifyCertificateAction() {
        self.presentation.wrappedValue.dismiss()
        
        certificate.validuntil = validuntil
        
        onChange(
            ModifyCertificateResponse(
                cert:certificate,
                selectedStatus:CertificateStatus(rawValue: Int8(selectedStatus))!,
                shouldDelete:false
            )
        )
    }
    
    private func deleteCertificateAction() {
        self.presentation.wrappedValue.dismiss()
        
        DispatchQueue.main.async {
            onChange(
                ModifyCertificateResponse(
                    cert: certificate,
                    selectedStatus: CertificateStatus(rawValue: Int8(selectedStatus))!,
                    shouldDelete: true
                )
            )
        }
    }
}


struct ModifyCertificate_Previews: PreviewProvider {
    
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        let photo = context.createPicture(image: UIImage(named: "passimg")!)
        
        let certificate = context.createCertificate(
            firstName:"Hugo",
            lastName:"Meier",
            phoneNumber:"08945566",
            email:"bla@fasel.de",
            status: CertificateStatus.unknown,
            type: CertificateType.rapidtest,
            pictureid: photo.id!
        )
        
        ModifyCertificate(
            certificate: certificate,
            certifcatePhoto: UIImage(named: "passimg")!,
            selectedStatus: Int(certificate.status)
        ) { _ in }
    }
}
