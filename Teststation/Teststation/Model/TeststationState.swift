import Foundation

final class TeststationState : ObservableObject {
    
    @Published var teststationId: String
    @Published var employeeId: String
    
    init(teststationId:String="", employeeId:String="") {
        self.teststationId = teststationId
        self.employeeId = employeeId
    }
}
