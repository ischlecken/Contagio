import SwiftUI

struct CertificateView: View {
    @State var showAlert = false
    let certificate: Certificate
    
    var body: some View {
        VStack {
            Text(certificate.lastname!).italic()
            Spacer(minLength: 20)
            Color(.blue)
            Spacer(minLength: 20)
            Button(certificate.phonenumber!) {
                showAlert = true
            }
            .alert(isPresented: $showAlert) {
                Alert(title:Text("bla"),
                      message: Text("msg is \(certificate.lastname!)"),
                      primaryButton: .destructive(Text("...Delete...")),
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
