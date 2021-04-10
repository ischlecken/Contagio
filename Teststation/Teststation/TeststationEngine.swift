import SwiftUI
import Combine
import CoreData

class TeststationEngine {
    
    static let shared = TeststationEngine()
    
    var contagioAPI : AnyCancellable?
    var passInfo: [PassInfo]?
    var error: TeststationError?
    
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
        
        contagioAPI = ContagioAPI
            .allPass()
            .sink(
                receiveCompletion: { [unowned self] completion in
                    switch completion {
                    case .finished: break
                    case .failure(let error):
                        print("Error: \(error)")
                        self.error = error
                    }
                },
                receiveValue: { [unowned self] result in
                    print("Thread: \(Thread.current)")
                    
                    self.passInfo = result
                    
                    print("result= \(result)")
                }
            )
        
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
