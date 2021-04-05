import SwiftUI

struct ModifyCertificate: View {
    
    static let releaseFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        
        return formatter
    }()
    
    @Environment(\.presentationMode) var presentation
    @State var certificate: Certificate
    @State var certifcatePhoto: UIImage
    @State var selectedStatus: Int
    @State var showCert: Bool = false
    
    let types:[Int8] = CertificateType.allCases.map{ $0.rawValue }
    let status:[Int8] = CertificateStatus.allCases.map{ $0.rawValue }
    let onChange: (Certificate,Int) -> Void
    
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
                            Text("certificatetype_\(certificate.type)".localized()).font(.caption2)
                            
                            HStack {
                                Text("addcert_validto").foregroundColor(Color(.gray)).font(.caption)
                                Text(Self.releaseFormatter.string(from: certificate.validto!)).font(.caption)
                            }
                        }
                        
                    }
                }
                Section()  {
                    VStack(alignment: .leading) {
                        Text("addcert_status").font(.caption)
                        Picker(selection: $selectedStatus, label: Text("Certification Status")) {
                            ForEach(status.indices) { i in
                                Text("certificatestatus_\(status[i])".localized()).tag(i)
                            }
                        }.pickerStyle(SegmentedPickerStyle())
                    }
                }
                Section() {
                    VStack(alignment: .leading) {
                        Text("addcert_phonenumber").font(.caption)
                        Text(certificate.phonenumber!)
                    }
                    VStack(alignment: .leading) {
                        Text("addcert_email").font(.caption)
                        Text(certificate.email!)
                    }
                }
                Section {
                    Button(action: modifyCertificateAction) {
                        Text("modifycert_updatebutton")
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
            
        }
        .navigationBarTitle(Text("modifycert_title"))
    }
    
    private func modifyCertificateAction() {
        self.presentation.wrappedValue.dismiss()
        onChange(certificate, selectedStatus)
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
            validTo: Date().advanced(by: 86400),
            status: CertificateStatus.unknown,
            type: CertificateType.rapidtest,
            pictureid: photo.id!
        )
        
        ModifyCertificate(
            certificate: certificate,
            certifcatePhoto: UIImage(named: "passimg")!,
            selectedStatus: Int(certificate.status)
        ) { certificate,selectedStatus in }
    }
}
