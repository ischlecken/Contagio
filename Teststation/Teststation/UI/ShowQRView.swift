import SwiftUI

struct ShowQRView: View {
    @State var certificate: Certificate
    
    var body: some View {
        VStack(alignment: .center) {
            Image(uiImage: generateQRCode(from: certificate.passURL()))
                .interpolation(.none)
                .resizable()
                .scaledToFit()
                .frame(width: 300, height: 300)
            Text("scan qr code")
        }
    }
    
}

struct ShowQRView_Previews: PreviewProvider {
    
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
        
        ShowQRView(certificate:certificate)
    }
}

