import SwiftUI
import Combine
import MessageUI

struct ModifyCertificate: View {
    
    @Environment(\.presentationMode) var presentation
    @StateObject var passLoader = PassLoaderService()
    
    @State var certificate: Certificate
    @State var certifcatePhoto: UIImage
    @State var selectedStatus: Int
    @State var validuntil: Date
    @State var showPassSend: Bool = false
    
    let types:[Int8] = CertificateType.allCases.map{ $0.rawValue }
    let status:[Int8] = CertificateStatus.allCases.map{ $0.rawValue }
    let onChange: (ModifyCertificateResponse) -> Void
    private let messageComposeDelegate = MessageDelegate()
    
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
                            
                            if( certificate.validuntil != nil) {
                                HStack {
                                    Text("addcert_validto").foregroundColor(Color(.gray)).font(.caption)
                                    Text(DateFormatter.certificate.string(from: certificate.validuntil!)).font(.caption).bold()
                                }}
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
                
                if( MFMessageComposeViewController.canSendText() ) {
                    Section {
                        Button(action: { presentMessageCompose(); } ) {
                            Text("modifycert_sendpass")
                        }
                        .disabled(!passLoader.passIsLoaded)
                    }
                    
                }
            }
            .listStyle(InsetGroupedListStyle())
            .onAppear {
                passLoader.startLoadingPass(passid: certificate.passid)
            }
        }
    }
    
    private class MessageDelegate: NSObject, MFMessageComposeViewControllerDelegate {
        func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
            controller.dismiss(animated: true)
        }
    }
    
    private func presentMessageCompose() {
        guard let pass = passLoader.pass, MFMessageComposeViewController.canSendText() else {
            return
        }
        let vc = UIApplication.shared.keyWindow?.rootViewController
        
        let composeVC = MFMessageComposeViewController()
        composeVC.messageComposeDelegate = messageComposeDelegate
        
        composeVC.recipients = [certificate.phonenumber!]
        composeVC.body = "sendpass.message".localized()
        
        composeVC.addAttachmentData(pass, typeIdentifier: "application/vnd.apple.pkpass", filename: "\(certificate.serialnumber!).pkpass")
        
        vc?.present(composeVC, animated: true)
    }
    
    private func modifyCertificateAction() {
        self.presentation.wrappedValue.dismiss()
        
        onChange(
            ModifyCertificateResponse(
                cert:certificate,
                selectedStatus: CertificateStatus(rawValue: Int8(selectedStatus))!,
                validuntil: validuntil
            )
        )
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
            pictureid: photo.id!,
            teststationid: "42",
            testerid: "43"
        )
        
        ModifyCertificate(
            certificate: certificate,
            certifcatePhoto: UIImage(named: "passimg")!,
            selectedStatus: Int(certificate.status),
            validuntil: Date()
        ) { _ in }
    }
}
