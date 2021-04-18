import SwiftUI

struct CertificateRow: View {
    
    static let defaultPhoto = UIImage(named: "passimg")!
    
    
    @ObservedObject
    var certificate: Certificate
    let photo: UIImage?
    
    var body: some View {
        let statusColor = certificate.certIssueStatus.isFinished() ? Color("backgroundcertificatestatus_\(certificate.status)") : Color.gray
        
        HStack {
            ZStack(alignment: .center) {
                Image(uiImage: photo ?? Self.defaultPhoto)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 80, height: 80, alignment: /*@START_MENU_TOKEN@*/.center/*@END_MENU_TOKEN@*/)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(statusColor, lineWidth: 4)
                    )
                    .shadow(radius: 4)
                
                if( !certificate.certIssueStatus.isFinished() ) {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.white))
                }
            }
            VStack(alignment: .leading) {
                Spacer()
                Text("\(certificate.firstname!) \(certificate.lastname!)").font(.title)
                Spacer()
                HStack {
                    let certType = "certificatetype_\(certificate.type)".localized()
                    let certStatus = "certificatestatus_\(certificate.status)".localized()
                    let certValid = certificate.validuntil != nil ? DateFormatter.certificate.string(from: certificate.validuntil!) : ""
                    
                    Text("certificaterow_status: \(certType) \(certStatus)").font(.caption).foregroundColor(statusColor).bold()
                    Spacer()
                    Text("validuntil: \(certValid)").font(.caption)
                }
                Spacer()
                let certIssueStatus = "certificateissuestatus_\(certificate.issuestatus)".localized()
                
                if( certificate.certIssueStatus.isFinished()){
                    Text("certificaterow_issuestatus: \(certIssueStatus)").font(.caption).bold()
                }
                else{
                    Text("certificaterow_issuestatus: \(certIssueStatus)").font(.caption).foregroundColor(statusColor)
                }
            }
        }
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
            status: CertificateStatus.negative,
            type: CertificateType.rapidtest,
            pictureid: photo.id!,
            teststationid: "32",
            testerid: "43"
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
