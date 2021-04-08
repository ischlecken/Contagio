import SwiftUI
import Combine
import CoreData

class TeststationEngine {
    
    static let shared = TeststationEngine()
    
    init() {
        print("TeststationEngine()")
    }
    
    
    func startIssueOfCertificate(mcr: ModifyCertificateResponse,
                                 onChange: @escaping (CertificateIssueStatus)->Void) {
        startIssueOfCertificate(certificate: mcr.cert, selectedStatus: mcr.selectedStatus, onChange: onChange)
    }
    
    func startIssueOfCertificate(certificate: Certificate,
                                 selectedStatus:CertificateStatus,
                                 onChange: @escaping (CertificateIssueStatus)->Void) {
        
        (UIApplication.shared.delegate as!AppDelegate).persistentContainer.performBackgroundTask { context in
            guard let cert = try?context.getCertificate(objectID: certificate.objectID) else {
                return
            }
            
            cert.updateStatus(status: selectedStatus)
            context.saveContext()
            
            print("\(Thread.current) TeststationEngine.startIssueOfCertificate(\(cert.id!))")
            
            sleep(4)
            
            cert.updateIssueStatus(issueStatus: CertificateIssueStatus.pending)
            context.saveContext()
            onChange(CertificateIssueStatus.pending)
            print("\(Thread.current) TeststationEngine.startIssueOfCertificate(\(cert.id!)) issueStatus=\(cert.issuestatus)")
            
            sleep(4)
            
            cert.updateIssueStatus(issueStatus: CertificateIssueStatus.signed)
            context.saveContext()
            print("\(Thread.current) TeststationEngine.startIssueOfCertificate(\(cert.id!)) issueStatus=\(cert.issuestatus)")
            onChange(CertificateIssueStatus.signed)
        }
        
    }
}
