import SwiftUI
import MessageUI

struct MessageView: UIViewControllerRepresentable {
    let phoneNo: String
    let data: Data
    
    func makeUIViewController(context: UIViewControllerRepresentableContext<MessageView>) -> MFMessageComposeViewController {
        let vc = MFMessageComposeViewController()
        
        vc.recipients = [phoneNo]
        vc.body = "Hier ist ihr Pass"
        
        vc.addAttachmentData(data, typeIdentifier: "application/vnd.apple.pkpass", filename: "bla.pkpass")
        
        vc.delegate = context.coordinator
        
        return vc
    }
    
    func updateUIViewController(_ uiViewController: MFMessageComposeViewController, context: UIViewControllerRepresentableContext<MessageView>) {
        
    }
    
    func makeCoordinator() -> MailDelegate {
        MailDelegate()
    }
    
    class MailDelegate: NSObject, UINavigationControllerDelegate, MFMessageComposeViewControllerDelegate {
        func messageComposeViewController(_ controller: MFMessageComposeViewController,
                                          didFinishWith result: MessageComposeResult) {
            
            controller.dismiss(animated: true)
        }
    }
}
