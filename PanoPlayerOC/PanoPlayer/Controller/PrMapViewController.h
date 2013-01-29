//
//  PrMapViewController.h
//  PanoPlayer
//
//  Created by yiluhao on 13-1-29.
//
//

#import <UIKit/UIKit.h>
#import "MTImageMapView.h"

@interface PrMapViewController : UIViewController <MTImageMapDelegate>{
    NSArray *coordsData;
    NSString *responseData;
    UIProgressView *imageProgressIndicator;
}

@property(nonatomic, retain) NSMutableArray *linkScene;

@property (nonatomic, assign) IBOutlet UIScrollView         *viewScrollStub;
@property (nonatomic, assign) IBOutlet MTImageMapView       *viewImageMap;
@property (nonatomic, retain)          NSArray              *stateNames;

@end
