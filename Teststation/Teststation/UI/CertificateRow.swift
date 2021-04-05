import SwiftUI

struct CertificateRow: View {
    
    static let defaultPhoto = UIImage(named: "passimg")!
    static let releaseFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        
        return formatter
    }()
    
    let certificate: Certificate
    let photo: UIImage?
        
    var body: some View {
        let statusColor = Color("backgroundcertificatestatus_\(certificate.status)")
        
        HStack {
            Image(uiImage: photo ?? Self.defaultPhoto)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 80, height: 80, alignment: /*@START_MENU_TOKEN@*/.center/*@END_MENU_TOKEN@*/)
                .clipShape(Circle())
                .overlay(Circle().stroke(Color.blue, lineWidth: 4))
            VStack(alignment: .leading) {
                Spacer()
                Text("\(certificate.firstname!) \(certificate.lastname!)").font(.title)
                Spacer()
                HStack {
                    let certType = "certificatetype_\(certificate.type)".localized()
                    let certStatus = "certificatestatus_\(certificate.status)".localized()
                    let certValid = certificate.validto != nil ? Self.releaseFormatter.string(from: certificate.validto!) : ""
                    
                    Text("certificaterow_status: \(certType) \(certStatus)").font(.caption).foregroundColor(statusColor).bold()
                    Spacer()
                    Text("validuntil: \(certValid)").font(.caption)
                }
                Spacer()
            }}
    }
}


struct CertificateRow_Previews: PreviewProvider {
    
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        let photo = context.createPicture(image: UIImage(named: "passimg")!)
    
        let certificate = context.createCertificate(
            firstName:"Hugo",
            lastName:"Meier",
            phoneNumber:"08945566",
            email:"bla@fasel.de",
            validTo: Date().advanced(by: 86400),
            status: CertificateStatus.negative,
            type: CertificateType.rapidtest,
            pictureid: photo.id!
        )
        
        Group {
            CertificateRow(certificate:certificate, photo: nil)
                .frame(height: 80)
                .previewLayout(PreviewLayout.sizeThatFits)
                .previewDisplayName("CertificateRow")
            
            CertificateRow(certificate:certificate, photo: nil)
                .frame(height: 80)
                .preferredColorScheme(.dark)
                .previewLayout(PreviewLayout.sizeThatFits)
                .previewDisplayName("CertificateRow")
        }
    }
}
