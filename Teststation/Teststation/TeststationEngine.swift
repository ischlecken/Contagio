import SwiftUI
import Combine
import CoreData

class TeststationEngine {
    
    static let shared = TeststationEngine()
    
    var contagioAPISubscription : AnyCancellable?
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
        
        let persistentContainer = (UIApplication.shared.delegate as!AppDelegate).persistentContainer
        
        certificate.updateIssueStatus(issueStatus:.pending)
        certificate.managedObjectContext?.saveContext()
        
        contagioAPISubscription = ContagioAPI
            .allPass()
            .sink(
                receiveCompletion: { [unowned self] completion in
                    
                    let context = persistentContainer.newBackgroundContext()
                    
                    guard let cert = try? context.getCertificate(objectID: certificate.objectID) else {
                        return
                    }
                    
                    sleep(4)
                    
                    switch completion {
                    case .finished:
                        cert.updateStatus(status: selectedStatus)
                        cert.updateIssueStatus(issueStatus: .signed)
                    case .failure(let error):
                        print("Error: \(error)")
                        self.error = error
                        
                        cert.updateIssueStatus(issueStatus: .failed)
                    }
                    
                    context.saveContext()
                    
                    contagioAPISubscription = nil
                },
                receiveValue: { [unowned self] result in
                    print("Thread: \(Thread.current) mainThread:\(Thread.current.isMainThread)")
                    
                    self.passInfo = result
                    
                    print("result= \(result)")
                }
            )
    }
    
}