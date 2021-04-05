import SwiftUI
import PassKit

struct PassView: UIViewControllerRepresentable {
    let pass: PKPass
    
    func makeUIViewController(context: UIViewControllerRepresentableContext<PassView>) -> PKAddPassesViewController {
        let addPassesViewController = PKAddPassesViewController(pass: pass)
        
        addPassesViewController!.delegate = context.coordinator
        
        return addPassesViewController!
    }
    
    func updateUIViewController(_ uiViewController: PKAddPassesViewController, context: UIViewControllerRepresentableContext<PassView>) {
        
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UINavigationControllerDelegate, PKAddPassesViewControllerDelegate {
        let parent: PassView
        
        init(_ parent: PassView) {
            self.parent = parent
        }
        
    }
}
