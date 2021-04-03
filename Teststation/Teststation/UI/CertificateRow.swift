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
        VStack(alignment: .leading) {
            HStack {certificate.firstname
                .map(Text.init)
                .font(.title)
                certificate.lastname
                    .map(Text.init)
                    .font(.title)
            }
            HStack {
                let certType = "certificatetype_\(certificate.type)".localized()
                let certStatus = "certificatestatus_\(certificate.status)".localized()
                let certValid = certificate.validto != nil ? Self.releaseFormatter.string(from: certificate.validto!) : ""
                
                Text("certificaterow_status: \(certType) \(certStatus)").font(.caption)
                Spacer()
                Text("validuntil: \(certValid)").font(.caption)
            }
        }
    }
}


struct CertificateRow_Previews: PreviewProvider {
    
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        
        let certificate = createCertificate(
            firstName:"Hugo",
            lastName:"Meier",
            phoneNumber:"08945566",
            context:context
        )
        
        CertificateRow(certificate:certificate)
            .previewLayout(PreviewLayout.sizeThatFits)
            .padding()
            .previewDisplayName("CertificateRow")
    }
}
