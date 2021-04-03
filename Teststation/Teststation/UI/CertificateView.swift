import SwiftUI

struct CertificateView: View {
    
    static let releaseFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateStyle = .short
    formatter.timeStyle = .short
    
    return formatter
}()
    
    @State var showAlert = false
    let certificate: Certificate
    
    var body: some View {
        VStack {
            Text("\(certificate.firstname!) \(certificate.lastname!)").font(.largeTitle)
            Spacer(minLength: 20)
            Text("certview_phonenumber: \(certificate.phonenumber!)").font(.caption)
            Text("certview_email: \(certificate.email!)").font(.caption)
            
            let certType = "certificatetype_\(certificate.type)".localized()
            let certStatus = "certificatestatus_\(certificate.status)".localized()
            let certValid = certificate.validto != nil ? Self.releaseFormatter.string(from: certificate.validto!) : ""
            
            Text("certificaterow_status: \(certType) \(certStatus)").font(.caption)
            Text("validuntil: \(certValid)").font(.caption)
            Spacer(minLength: 20)
            
            Button("certview_delete") {
                showAlert = true
            }.font(.title)
            .alert(isPresented: $showAlert) {
                Alert(title:Text("alert_deletecertificate_title"),
                      message: Text("alert_deletecertificate_message"),
                      primaryButton: .destructive(Text("alert_deletecertificate_deletebutton")),
                      secondaryButton: .cancel()
                )
            }
        }
    }
}

struct CertificateView_Previews: PreviewProvider {
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        
        let certificate = createCertificate(
            firstName:"Hugo",
            lastName:"Meier",
            phoneNumber:"08945566",
            email:"bla@fasel.de",
            validTo: Date().advanced(by: 86400),
            status: CertificateStatus.positive,
            type: CertificateType.rapidtest,
            context:context
        )
        
        CertificateView(certificate: certificate)
    }
}
