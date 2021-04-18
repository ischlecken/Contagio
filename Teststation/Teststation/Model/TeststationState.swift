import Foundation
import Combine

final class TeststationState : ObservableObject {
    @Published var teststation: Teststation? = nil
    @Published var tester: Tester? = nil
    
    var allTeststations: [Teststation]? = nil
    var allTester: [Tester]? = nil
    
    var loadTeststationSubscription: AnyCancellable? = nil
    var loadTesterSubscription: AnyCancellable? = nil
    
    func loadTeststationInfo() {
        if( loadTeststationSubscription == nil ) {
            loadTeststationSubscription = ContagioAPI
                .getTeststations()
                .sink(
                    receiveCompletion: { [unowned self]completion in
                        switch completion {
                        case .finished:
                            break
                        case .failure(let error):
                            print("Error: \(error)")
                        }
                        
                        loadTeststationSubscription = nil
                    },
                    receiveValue: { [unowned self] result in
                        allTeststations = result
                    }
                )
        }
    }
    
    func loadTesterInfo() {
        if( loadTesterSubscription == nil ) {
            loadTesterSubscription = ContagioAPI
                .getTester()
                .sink(
                    receiveCompletion: { [unowned self]completion in
                        switch completion {
                        case .finished:
                            break
                        case .failure(let error):
                            print("Error: \(error)")
                        }
                        
                        loadTesterSubscription = nil
                    },
                    receiveValue: { [unowned self] result in
                        allTester = result
                    }
                )
        }
    }
    
    func login(user:String, password:String) {
        
        if let tester0 = allTester?.first(where: {$0.id == user}) {
            print("found tester: \(tester0)")
            
            if let teststation0 = allTeststations?.first(where: { $0.id == tester0.teststationId }) {
                print("found teststation: \(teststation0)")
                
                if( password == "123" ) {
                    tester = tester0
                    teststation = teststation0
                }
            }
        }
    }
    
    func mockLogin() -> TeststationState {
        teststation = Teststation(id: "1",
                                  name: "München",
                                  address: Address(city: "München", zipcode: "81673", street: "Willy-Brandt", hno: "2")
        )
        
        tester = Tester(id: "1",
                        teststationId: "1",
                        person: Person(firstName: "Hugo", lastName: "Maier", phoneNo: "08934334", email: "hugo@testzentrum.de")
        )
        
        return self
    }
}
