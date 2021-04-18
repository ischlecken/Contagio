import SwiftUI
import Combine

class PassLoaderService: ObservableObject {
    @Published var pass: Data? = nil
    @Published var isLoading: Bool = false
    @Published var passIsLoaded: Bool = false
    
    var loadPassSubscription: AnyCancellable? = nil
    
    func startLoadingPass(passid: String?) {
        if let passid = passid {
            isLoading = false
            
            if( pass != nil ) {
                passIsLoaded = true
                
                return
            }
            
            if( loadPassSubscription == nil ) {
                isLoading = true
                
                loadPassSubscription = try? ContagioAPI
                    .getPass(passId: passid)
                    .receive(on: DispatchQueue.main)
                    .sink(
                        receiveCompletion: { [unowned self]completion in
                            switch completion {
                            case .finished:
                                passIsLoaded = true
                            case .failure(let error):
                                print("Error: \(error)")
                            }
                            
                            isLoading = false
                            loadPassSubscription = nil
                        },
                        receiveValue: { [unowned self]result in
                            pass = result
                        }
                    )
            }
        }
    }
}
