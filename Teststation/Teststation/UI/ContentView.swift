import SwiftUI
import CoreData

struct ContentView: View {
    @EnvironmentObject var teststationState: TeststationState
    
    var body: some View {
        if( teststationState.employeeId.isEmpty ) {
            LoginView()
        } else {
            CertificateList()
        }
    }
    
}

struct ContentView_Previews: PreviewProvider {
    
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        
        Group {
            ContentView()
                .environment(\.managedObjectContext, context)
                .environmentObject(TeststationSettings())
                .environmentObject(TeststationState(teststationId: "Tübingen", employeeId: "ced"))
        }
    }
}
