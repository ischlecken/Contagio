import SwiftUI

struct CertificateRow: View {
    let certificate: Certificate
    
    static let releaseFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        
        return formatter
    }()
    
    var body: some View {
        let statusColor = Color("backgroundcertificatestatus_\(certificate.status)")
        
        HStack {
            Image("passimg").resizable().aspectRatio(contentMode: .fit)
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
        
        let certificate = createCertificate(
            firstName:"Hugo",
            lastName:"Meier",
            phoneNumber:"08945566",
            email:"bla@fasel.de",
            validTo: Date().advanced(by: 86400),
            status: CertificateStatus.negative,
            type: CertificateType.rapidtest,
            context:context
        )
        
        Group {
            CertificateRow(certificate:certificate)
                .frame(height: 80)
                .previewLayout(PreviewLayout.sizeThatFits)
                .previewDisplayName("CertificateRow")
            CertificateRow(certificate:certificate)
                .frame(height: 80)
                .preferredColorScheme(.dark)
                .previewLayout(PreviewLayout.sizeThatFits)
                .previewDisplayName("CertificateRow")
        }
    }
}
