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
        //startIssueOfCertificate(certificate: mcr.cert, selectedStatus: mcr.selectedStatus, onChange: onChange)
    }
    
    func startIssueOfCertificate(certificate: Certificate,
                                 selectedStatus:CertificateStatus,
                                 photo: UIImage,
                                 onChange: @escaping (CertificateIssueStatus)->Void) {
        
        let persistentContainer = (UIApplication.shared.delegate as!AppDelegate).persistentContainer
        
        certificate.updateIssueStatus(issueStatus:.pending)
        certificate.managedObjectContext?.saveContext()
        
        let createPassRequest = CreatePassRequest(
            firstName: certificate.firstname!,
            lastName: certificate.lastname!,
            phoneNo: certificate.phonenumber!,
            email: certificate.email,
            teststationId: certificate.teststationid!,
            testerId: certificate.testerid!,
            testResult: TestResultType.fromCertificateStatus(status: selectedStatus),
            testType: TestType.fromCertificateType(type: certificate.certType),
            photo: photo.pngData()!
        )
        
        contagioAPISubscription = try? ContagioAPI
            .createPass(createPassRequest: createPassRequest)
            .sink(
                receiveCompletion: { [unowned self] completion in
                    switch completion {
                    case .finished:
                        break
                    case .failure(let error):
                        print("Error: \(error)")
                        self.error = error
                        
                        let context = persistentContainer.newBackgroundContext()
                        guard let cert = try? context.getCertificate(objectID: certificate.objectID) else {
                            return
                        }
                        
                        cert.updateIssueStatus(issueStatus: .failed)
                        context.saveContext()
                    }
                
                    contagioAPISubscription = nil
                },
                receiveValue: { result in
                    let context = persistentContainer.newBackgroundContext()
                    guard let cert = try? context.getCertificate(objectID: certificate.objectID) else {
                        return
                    }
                    
                    print("result=\(result)")
                    
                    sleep(4)
                    
                    cert.pass = result
                    cert.updateStatus(status: selectedStatus)
                    cert.updateIssueStatus(issueStatus: .signed)
                    context.saveContext()
                }
            )
    }
    
}
