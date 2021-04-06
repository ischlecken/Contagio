import SwiftUI
import Combine
import CoreData

class TeststationEngine {
    
    init() {
        print("TeststationEngine()")
    }
    
    func startIssueOfCertificate(certificate:Certificate,  onChange: @escaping (String,CertificateIssueStatus)->Void ) {
        print("TeststationEngine.startIssueOfCertificate(\(certificate.id!))")
        
        (UIApplication.shared.delegate as!AppDelegate).persistentContainer.performBackgroundTask { context in
            let cid = certificate.id!
            let newCert = context.getCertificate(id:cid)!
            
            sleep(4)
            
            context.updateCertificateIssueStatus(certificate: newCert, issueStatus: CertificateIssueStatus.pending)
            
            DispatchQueue.main.async {
                onChange(cid,CertificateIssueStatus.pending)
            }
            sleep(4)
            
            context.updateCertificateIssueStatus(certificate: newCert, issueStatus: CertificateIssueStatus.signed)
            DispatchQueue.main.async {
                onChange(cid,CertificateIssueStatus.signed)
            }
            
        }
        
    }
}
