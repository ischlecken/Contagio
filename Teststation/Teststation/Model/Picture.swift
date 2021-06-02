import SwiftUI
import CoreData

extension NSManagedObjectContext {
    
    func createPicture(image: UIImage) -> Picture {
        let result = Picture(context: self)
        
        result.id = UUID().uuidString
        result.createts = Date()
        result.format = "image/png"
        result.data = image.pngData()
        
        return result
    }


    func loadPictures(certificates: FetchedResults<Certificate>) -> [String : UIImage] {
        let fetchRequest = NSFetchRequest<Picture>(entityName: "Picture")
        var newCertPhotos = [String: UIImage]()
        
        for c in certificates {
            if let picid = c.pictureid {
                print("loadPictures(\(c.id!),pictid=\(picid))...")
                
                fetchRequest.predicate = NSPredicate(format: "id == %@", picid)
                
                do {
                    let pictures = try self.fetch(fetchRequest)
                    
                    print("loadPictures(\(picid)): \(pictures.count)")
                    for picture in pictures {
                        newCertPhotos[c.id!] = UIImage(data: picture.data!)
                    }
                }
                catch let error as NSError {
                    print("could not fetch \(error), \(error.userInfo)")
                }
            }
        }
        
        return newCertPhotos
    }
    
}

